package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义View方式三:重新绘制,继承View
 * 第一步:画出外圆drawArc
 * 第二步:画出进度圆drawArc
 * 第三步:画出文本:中间文本,顶部文本,底部文本drawText
 * Created by cjy on 17/6/14.
 */
public class ArcCircleView extends View {
    private Context mContext;
    /**
     * 文本画笔
     */
    private Paint mTextPaint;
    /**
     * 圆弧画笔
     */
    private Paint mArcCirclePaint;

    /**
     * 弧度
     */
    private int mAngleValue = 270;
    /**
     * 圆的背景色:默认浅绿色
     */
    private int mCircleBackgroundColor = Color.TRANSPARENT;
    /**
     * 进度的颜色,默认白色
     */
    private int mProgressColor = 0xffffffff;
    /**
     * 边宽
     */
    private int mStrokeWidth = 8;
    /**
     * 进度圆边宽
     */
    private int mInnerStrokeWidth = 7;

    public ArcCircleView(Context context) {
        super(context);
        init(context);
    }

    public ArcCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ArcCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        mContext = context;

        mTextPaint  = new Paint();
        //设置抗锯齿
        mTextPaint.setAntiAlias(true);
        //使文本看起来更清晰
        mTextPaint.setLinearText(true);

        mArcCirclePaint  = new Paint();
        mArcCirclePaint.setAntiAlias(true);
        mArcCirclePaint.setStyle(Paint.Style.STROKE);

    }

    public void setProgressColor(int progressColor) {
        this.mProgressColor = mContext.getResources().getColor(progressColor);
        invalidate();
    }

    public void setCircleBackgroundColor(int circleBackgroundColor) {
        this.mCircleBackgroundColor = mContext.getResources().getColor(circleBackgroundColor);
        invalidate();
    }

    public void setStrokeWidth(int strokeWidth){
        this.mStrokeWidth = strokeWidth;
        invalidate();
    }

    public void setInnerStrokeWidth(int innerStrokeWidth){
        this.mInnerStrokeWidth = innerStrokeWidth;
        invalidate();
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(float progress){
        int angleValue = (int) ((progress * 1.0)/100 * 360);
        if (angleValue != 0 && progress <= 100){
            mAngleValue  = angleValue;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //1-圆弧的位置:整圆,再绘制进度圆弧
        mArcCirclePaint.setColor(mCircleBackgroundColor);
        mArcCirclePaint.setStrokeWidth(mStrokeWidth);
        //屏幕宽度
        int width = getMeasuredWidth();
        RectF rectF = new RectF();
        rectF.left = mStrokeWidth;
        rectF.top = mStrokeWidth;
        rectF.right = width - mStrokeWidth;
        rectF.bottom = width - mStrokeWidth;
        if ((rectF.right - rectF.left) > (rectF.bottom- rectF.top)){//正方形矩形,保证画出的圆不会变成椭圆
            float space = (rectF.right - rectF.left) - (rectF.bottom- rectF.top);
            rectF.left += space/2;
            rectF.right -= space/2;
        }
//        canvas.drawArc(rectF,270,360,false,mArcCirclePaint);//第2个参数:时钟3点处为0度,逆时针为正方向

        mArcCirclePaint.setColor(mProgressColor);
        //设置边角为圆
        mArcCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mArcCirclePaint.setStrokeWidth(mInnerStrokeWidth);
        canvas.drawArc(rectF,270,mAngleValue,false,mArcCirclePaint);
    }

}
