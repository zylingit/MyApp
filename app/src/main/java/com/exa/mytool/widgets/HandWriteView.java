package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by 江坚 on 2020/9/10
 * Description：
 */
public class HandWriteView extends RelativeLayout {
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private static short[] mTracks;
    private static int mCount;

    /**
     * @param context
     */
    public HandWriteView(Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public HandWriteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initData();
    }

    private void initData() {
        mTracks = new short[1024*10];
        mCount = 0;
    }

    private void init() {
        //this.setBackgroundColor(Color.BLACK);
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }

    private void touchStart(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mTracks[mCount++] = (short) x;
        mTracks[mCount++] = (short) y;
    }

    private void touchMove(float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }

        mTracks[mCount++] = (short) x;
        mTracks[mCount++] = (short) y;
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
        mTracks[mCount++] = -1;
        mTracks[mCount++] = 0;
        writeComplete();
    }

    private void writeComplete() {
        short[] mTracksTemp;
        int countTemp = mCount;
        mTracksTemp = mTracks.clone();
        mTracksTemp[countTemp++] = -1;
        mTracksTemp[countTemp++] = -1;
        if (listener != null) {
            listener.onWriteCompleted(mTracksTemp);
        }
    }


    private HandWriteListener listener;

    public interface HandWriteListener {
        void onWriteCompleted(short[] mTracksTemp);
    }

    public void setHandWriteListener(HandWriteListener listener) {
        this.listener = listener;
    }

    public void reset() {
        mCount = 0;
        mPath.reset();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }


}
