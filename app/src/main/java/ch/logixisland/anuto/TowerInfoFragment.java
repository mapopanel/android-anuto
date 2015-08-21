package ch.logixisland.anuto;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

import ch.logixisland.anuto.game.GameManager;
import ch.logixisland.anuto.game.objects.Tower;

public class TowerInfoFragment extends Fragment implements
        View.OnTouchListener, View.OnClickListener,
        GameManager.OnShowTowerInfoListener, GameManager.OnHideTowerInfoListener,
        GameManager.OnCreditsChangedListener {

    private Handler mHandler;
    private GameManager mManager;

    private Tower mTower;

    private TextView txt_value;
    private TextView txt_reload;
    private TextView txt_damage;
    private TextView txt_range;

    private Button btn_upgrade;
    private Button btn_enhance;
    private Button btn_sell;

    private TowerView view_tower;

    private boolean mVisible = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tower_info, container, false);

        txt_value = (TextView)v.findViewById(R.id.txt_value);
        txt_reload = (TextView)v.findViewById(R.id.txt_reload);
        txt_damage = (TextView)v.findViewById(R.id.txt_damage);
        txt_range = (TextView)v.findViewById(R.id.txt_range);

        btn_upgrade = (Button)v.findViewById(R.id.btn_upgrade);
        btn_enhance = (Button)v.findViewById(R.id.btn_enhance);
        btn_sell = (Button)v.findViewById(R.id.btn_sell);

        view_tower = (TowerView)v.findViewById(R.id.view_tower);

        btn_upgrade.setOnClickListener(this);
        btn_sell.setOnClickListener(this);
        btn_enhance.setOnClickListener(this);

        view_tower.setEnabled(false);

        mHandler = new Handler();

        return v;
    }

    private void show() {
        if (!mVisible) {
            getFragmentManager().beginTransaction()
                    .show(this)
                    .commit();

            mVisible = true;
        }
    }

    private void hide() {
        if (mVisible) {
            getFragmentManager().beginTransaction()
                    .hide(this)
                    .commit();

            mVisible = false;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        hide();

        mManager = GameManager.getInstance();
        mManager.addListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        view_tower.close();
        mManager.removeListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == btn_upgrade) {
            mTower = mTower.upgrade();
            mManager.setSelectedTower(mTower);
            mManager.showTowerInfo(mTower);
        }

        if (v == btn_sell) {
            view_tower.setTower(null);

            mTower.sell();
            mTower.remove();
            mTower = null;

            mManager.hideTowerInfo();
        }

        if (v == btn_enhance) {
            mTower.enhance();
            mManager.showTowerInfo(mTower);
        }
    }

    @Override
    public void onShowTowerInfo(Tower tower) {
        mTower = tower;
        view_tower.setTower(mTower);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                DecimalFormat fmt = new DecimalFormat("#.#");

                txt_value.setText(fmt.format(mTower.getValue()));
                txt_damage.setText(fmt.format(mTower.getDamage()) + " (+" + fmt.format(mTower.getConfig().enhanceDamage) + ")");
                txt_range.setText(fmt.format(mTower.getRange()) + " (+" + fmt.format(mTower.getConfig().enhanceRange) + ")");
                txt_reload.setText(fmt.format(mTower.getReloadTime()) + " (-" + fmt.format(mTower.getConfig().enhanceReload) + ")");

                if (mTower.isUpgradeable()) {
                    btn_upgrade.setText(getResources().getString(R.string.upgrade) + " (" + mTower.getUpgradeCost() + ")");
                } else {
                    btn_upgrade.setText(getResources().getString(R.string.upgrade));
                }

                btn_enhance.setText(getResources().getString(R.string.enhance) + " (" + mTower.getEnhanceCost() + ")");

                show();
            }
        });

        onCreditsChanged(mManager.getCredits());
    }

    @Override
    public void onHideTowerInfo() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        });
    }

    @Override
    public void onCreditsChanged(final int credits) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                btn_upgrade.setEnabled(mTower != null && mTower.isUpgradeable() && credits >= mTower.getUpgradeCost());
                btn_enhance.setEnabled(mTower != null && credits >= mTower.getEnhanceCost());
            }
        });
    }
}