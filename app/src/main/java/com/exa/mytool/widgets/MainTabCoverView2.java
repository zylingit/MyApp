package com.aam.mida.mida_yk.widgets;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

import com.aam.mida.mida_yk.R;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * 首页tab 图片view
 */
public class MainTabCoverView2 extends AppCompatImageView {

    public static final int DEFAULT_SHADOW_COLOR = 0xaa000000;
    public int SHADOW_COLOR = DEFAULT_SHADOW_COLOR;
    public static final int ROTATE_DURATION = 300;
    private static final int MAX_PROGRESS = 100;
    private int mHeight;
    private int mWidth;
    private Bitmap bitmap;
    private Canvas tempCanvas;
    private Paint transparentPaint;
    private float mOuterCircleRadius = -1;//外边圆半径
    private float mInnerCircleRadius = -1;//内边圆半径
    private float mPauseCircleRadius = -1;
    private float mPauseMaxCircleRadius = -1;
    private float mCornerRadius = -1;
    private int mArcStart;
    private ValueAnimator mRotateAnimator;
    private float mPauseIconHeight;
    private float mPauseIconWidth;
    private float mPauseIconGap;
    private boolean mPausing;
    private ValueAnimator mPauseAnimator;
    private ValueAnimator mResumeAnimator;
    private boolean mStart = false;
    private float mInitOuterCircleRadius;
    private int mProgress;
    private int mPendingProgress;
    private OnPauseResumeListener mOnPauseResumeListener;

    private boolean isEnableLoadingProgress = false;

//    private Bitmap notDownLoadBitmap;
//    private Paint notDownLoadPaint;

    public MainTabCoverView2(Context context) {
        super(context);
        init(context, null);
    }

    public MainTabCoverView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public void setEnableLoadingProgress(boolean enable) {
        isEnableLoadingProgress = enable;
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CoverView);
            int color = typedArray.getColor(R.styleable.CoverView_cover_background, DEFAULT_SHADOW_COLOR);
            mCornerRadius = typedArray.getDimension(R.styleable.CoverView_corner_radius, -1);
            typedArray.recycle();
        }

        resetValues();

        mPauseAnimator = ValueAnimator.ofFloat(0.001f, 1);
        int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mPauseAnimator.setDuration(duration);
        mPauseAnimator.addUpdateListener(mPauseUpdateListener);
        mPauseAnimator.addListener(mPauseListener);
        mPauseAnimator.setInterpolator(new DecelerateInterpolator());

        mResumeAnimator = ValueAnimator.ofFloat(1, 0.001f);
        mResumeAnimator.setDuration(duration);
        mResumeAnimator.addUpdateListener(mResumeUpdateListener);
        mResumeAnimator.addListener(mResumeListener);
        mResumeAnimator.setInterpolator(new AccelerateInterpolator());

//        notDownLoadBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_main_tab_uninstall).copy(Bitmap.Config.ARGB_8888, true);
//        notDownLoadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private ValueAnimator getRotateAnimator(int prevProgress, int progress) {
        ValueAnimator rotateAnimator = ValueAnimator.ofInt(progressToDegress(prevProgress),
                progressToDegress(progress));
        rotateAnimator.setDuration(ROTATE_DURATION);
        rotateAnimator.addUpdateListener(mRotateListener);
        rotateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mStart = true;
                mPausing = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                handlePendingProgress();

                if (isFinished() && isEnableLoadingProgress) {
                    getFinishAnimator().start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        return rotateAnimator;
    }

    private void handlePendingProgress() {
        if (mPendingProgress != mProgress && mPendingProgress > mProgress) {
            setProgress(mPendingProgress);
        }
    }

    private int progressToDegress(float progress) {
        return (int) (360 * (progress / MAX_PROGRESS) - 90);
    }

    private ValueAnimator getFinishAnimator() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mInitOuterCircleRadius, mInitOuterCircleRadius * 2);
        valueAnimator.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mOuterCircleRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                invalidate();
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mPausing = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mStart = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return valueAnimator;
    }

    public void resetValues() {
        mProgress = 0;
        mPendingProgress = 0;

        mOuterCircleRadius = -1;
        mInnerCircleRadius = -1;
        mPauseCircleRadius = -1;
        mPauseMaxCircleRadius = -1;
    }

    private void initSizes(int width, int height) {
        int size = width < height ? width : height;
        mPauseIconHeight = 45f / 200 * size;
        mPauseIconWidth = 10f / 200 * size;
        mPauseIconGap = 10f / 200 * size;

        mInitOuterCircleRadius = 70f / 200 * size;

        if (mInnerCircleRadius == -1) {

            mInnerCircleRadius = 60f / 200 * size;
        }

        if (mCornerRadius == -1) {
            mCornerRadius = 20f / 200 * size;
        }

        if (mPauseMaxCircleRadius == -1) {
            mPauseMaxCircleRadius = mInnerCircleRadius * 0.7f;
        }

        if (mOuterCircleRadius == -1) {
            mOuterCircleRadius = mInitOuterCircleRadius;
        }

        if (mPauseCircleRadius == -1) {
            mPauseCircleRadius = mPauseMaxCircleRadius;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isEnableLoadingProgress) {
            return;
        }
        initSizes(getWidth(), getHeight());

        if (!mStart) {
            return;
        }

        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        tempCanvas = new Canvas(bitmap);
        tempCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        transparentPaint = new Paint();
        transparentPaint.setAntiAlias(true);
        transparentPaint.setDither(true);
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mHeight = getHeight();
        mWidth = getWidth();

        if (mProgress != 0) {
            //未下载的阴影
            tempCanvas.drawColor(SHADOW_COLOR);
        }

        int cx = mWidth / 4;
        int cy = mHeight / 2;

        //背景明亮的圆形
        if (mProgress != 0) {
            tempCanvas.drawCircle(cx, cy, mOuterCircleRadius, transparentPaint);
        }

        //动态绘制的阴影圆弧
        Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setDither(true);
        shadowPaint.setColor(SHADOW_COLOR);
        RectF rectF = new RectF(cx - mInnerCircleRadius,
                cy - mInnerCircleRadius,
                cx + mInnerCircleRadius,
                cy + mInnerCircleRadius);
        if (mProgress != 0) {
            tempCanvas.drawArc(rectF,
                    mArcStart,
                    270 - mArcStart,
                    true,
                    shadowPaint);
        }

        //未下载时候，绘制圆角
        if (mCornerRadius != 0) {
            Path path = new Path();
            path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), mCornerRadius, mCornerRadius, Path.Direction.CCW);
            canvas.clipPath(path);
        }

        canvas.drawBitmap(bitmap, 0, 0, null);

//        canvas.drawBitmap(notDownLoadBitmap, (mWidth - notDownLoadBitmap.getWidth()), 0, notDownLoadPaint);

//        if (mProgress != 0) {
//            canvas.drawBitmap(setTextToBitmap(), 0, 0, null);
//        }

        /**
         * Draw pause icon.
         */
        if (mPausing && mPauseCircleRadius * 2 > 1) {
            tempCanvas.drawCircle(cx, cy, mPauseCircleRadius, transparentPaint);

            Bitmap pauseBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas pauseCanvas = new Canvas(pauseBitmap);

            pauseCanvas.drawCircle(cx, cy, mPauseCircleRadius, shadowPaint);

            Paint gp1 = new Paint(transparentPaint);
            gp1.setAntiAlias(true);
            // 防抖动
            gp1.setDither(true);
            gp1.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            //Draw pause1.
            int pcx = (int) (cx - mPauseIconGap / 2 - mPauseIconWidth / 2);
            int pcy = (int) (cy);

            RectF pause1 = new RectF();
            pause1.left = pcx - mPauseIconWidth / 2;
            pause1.right = pcx + mPauseIconWidth / 2;
            pause1.top = pcy - mPauseIconHeight / 2;
            pause1.bottom = pcy + mPauseIconHeight / 2;
            pauseCanvas.drawRect(pause1, gp1);

            //Draw pause2.
            int pcx2 = (int) (cx + mPauseIconGap / 2 + mPauseIconWidth / 2);
            int pcy2 = (int) (cy);

            RectF pause2 = new RectF();
            pause2.left = pcx2 - mPauseIconWidth / 2;
            pause2.right = pcx2 + mPauseIconWidth / 2;
            pause2.top = pcy2 - mPauseIconHeight / 2;
            pause2.bottom = pcy2 + mPauseIconHeight / 2;
            pauseCanvas.drawRect(pause2, gp1);

            canvas.drawBitmap(pauseBitmap, 0, 0, null);
        }
    }

    /**
     * 文字绘制在图片上，并返回bitmap对象
     * 绘制进度
     */
    private Bitmap setTextToBitmap() {
        int width = getWidth();
        int height = getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        // 抗锯齿
        paint.setAntiAlias(true);
        // 防抖动
        paint.setDither(true);
        paint.setTextSize(40);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.parseColor("#FFFFFF"));
        float textWidth = paint.measureText(mProgress + "%");
        int cx = (int) ((width - textWidth) / 2);
        int cy = height / 2;
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float y = cy + (Math.abs(fontMetrics.ascent) - fontMetrics.descent) / 2;
        canvas.drawText(mProgress + "%", cx, y, paint);
        return bitmap;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            if (mStart) {
//                if (mPausing) {
//                    resumeLoading();
//                } else {
//                    pauseLoading();
//                }
//            }
//            return true;
//        }
//
//        return true;
//    }

    public void pauseLoading() {
        if (!mResumeAnimator.isRunning() && !mPauseAnimator.isRunning()) {
            mPausing = true;
            mPauseAnimator.start();
        }
    }


    public void resumeLoading() {
        if (!mPauseAnimator.isRunning() && !mResumeAnimator.isRunning()) {
            mPausing = true;
            mPauseAnimator.cancel();
            mResumeAnimator.start();
            handlePendingProgress();
        }
    }

    private ValueAnimator.AnimatorUpdateListener mResumeUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mPauseCircleRadius = mPauseMaxCircleRadius * ((Float) valueAnimator.getAnimatedValue()).floatValue();
            invalidate();
        }
    };

    private Animator.AnimatorListener mPauseListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            mPausing = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };


    private ValueAnimator.AnimatorUpdateListener mRotateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mArcStart = ((Integer) animation.getAnimatedValue()).intValue();
            invalidate();
        }
    };

    private ValueAnimator.AnimatorUpdateListener mPauseUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float v = ((Float) animation.getAnimatedValue()).floatValue();
            System.out.println("v = " + v);
            mPauseCircleRadius = mPauseMaxCircleRadius * v;
            invalidate();
        }
    };

    private Animator.AnimatorListener mResumeListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mPausing = false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };


    public void setProgress(int p) {
        if (p > MAX_PROGRESS) {
            p = MAX_PROGRESS;
        }

        if (p < mProgress) {
            mProgress = p;
            mPendingProgress = p;
            mOuterCircleRadius = mInitOuterCircleRadius;
            mStart = true;
            invalidate();
            mStart = false;
            return;
        }

        if ((mRotateAnimator != null && mRotateAnimator.isRunning()) || mPausing) {
            mPendingProgress = p;
            return;
        }

        int prevProgress = mProgress;
        mProgress = p;

        if (mRotateAnimator != null) {
            mRotateAnimator.cancel();
        }

        mRotateAnimator = getRotateAnimator(prevProgress, p);
        mRotateAnimator.start();
    }

    public int getProgress() {
        return mProgress;
    }

    public boolean isFinished() {
        return getProgress() == MAX_PROGRESS;
    }

    public OnPauseResumeListener getOnPauseResumeListener() {
        return mOnPauseResumeListener;
    }

    public void setOnPauseResumeListener(OnPauseResumeListener onPauseResumeListener) {
        mOnPauseResumeListener = onPauseResumeListener;
    }


    public interface OnPauseResumeListener {
        public void onPause();

        public void onResume();
    }
}
