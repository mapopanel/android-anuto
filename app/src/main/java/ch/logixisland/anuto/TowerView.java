package ch.logixisland.anuto;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import ch.logixisland.anuto.game.GameManager;
import ch.logixisland.anuto.game.objects.Tower;

public class TowerView extends View implements View.OnTouchListener {

    private final static float DRAW_SIZE = 1.3f;

    private Tower mTower;
    private Class<? extends Tower> mTowerClass;
    private GameManager mManager;

    private final Paint mPaintText;
    private final Matrix mScreenMatrix;

    private GameManager.Listener mCreditsListener = new GameManager.OnCreditsChangedListener() {
        @Override
        public void onCreditsChanged(int credits) {
            if (mTower != null) {
                if (credits >= mTower.getValue()) {
                    mPaintText.setColor(Color.BLACK);
                } else {
                    mPaintText.setColor(Color.RED);
                }

                TowerView.this.postInvalidate();
            }
        }
    };

    public TowerView(Context context, AttributeSet attrs) throws ClassNotFoundException{
        super(context, attrs);

        float density = context.getResources().getDisplayMetrics().density;
        mPaintText = new Paint();
        mPaintText.setColor(Color.BLACK);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setTextSize(25f * density);

        mScreenMatrix = new Matrix();

        if (!isInEditMode()) {
            mManager = GameManager.getInstance();
            mManager.addListener(mCreditsListener);
        }

        setOnTouchListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mScreenMatrix.reset();

        float tileSize = Math.min(w, h);
        mScreenMatrix.postTranslate(DRAW_SIZE / 2, DRAW_SIZE / 2);
        mScreenMatrix.postScale(tileSize / DRAW_SIZE, tileSize / DRAW_SIZE);

        float paddingLeft = (w - tileSize) / 2f;
        float paddingTop = (h - tileSize) / 2f;
        mScreenMatrix.postTranslate(paddingLeft, paddingTop);

        mScreenMatrix.postScale(1f, -1f);
        mScreenMatrix.postTranslate(0, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mTower != null) {
            canvas.save();
            canvas.concat(mScreenMatrix);
            canvas.translate(-mTower.getPosition().x, -mTower.getPosition().y);
            mTower.preview(canvas);
            canvas.restore();

            if (isEnabled()) {
                canvas.drawText(Integer.toString(mTower.getValue()),
                        getWidth() / 2,
                        getHeight() / 2 - (mPaintText.ascent() + mPaintText.descent()) / 2,
                        mPaintText);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isEnabled() && mTower != null && mManager.getCredits() >= mTower.getValue()) {
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder() {
                    @Override
                    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
                    }

                    @Override
                    public void onDrawShadow(Canvas canvas) {
                    }
                };

                ClipData data = ClipData.newPlainText("", "");
                startDrag(data, shadowBuilder, mTower, 0);
                newTower();
            }
        }

        return false;
    }


    public Class<? extends Tower> getTowerClass() {
        return mTowerClass;
    }

    public void setTowerClass(Class<? extends Tower> clazz) {
        mTowerClass = clazz;
        newTower();
    }

    public void setTowerClass(String className) throws ClassNotFoundException {
        mTowerClass = (Class<? extends Tower>) Class.forName(className);
        newTower();
    }

    public void setTower(Tower tower) {
        mTower = tower;
        this.postInvalidate();
    }


    public void close() {
        mManager.removeListener(mCreditsListener);
    }


    private void newTower() {
        try {
            mTower = mTowerClass.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError("Class " + mTowerClass.getName() + " has no default constructor!");
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate object!", e);
        }

        this.postInvalidate();
    }
}