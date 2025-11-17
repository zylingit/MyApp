package com.aam.mida.mida_yk.widgets;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class MovingImageView extends AppCompatImageView {
    private ObjectAnimator animator;

    public MovingImageView(Context context) {
        super(context);
        init();
    }

    public MovingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 创建属性动画对象
        animator = ObjectAnimator.ofFloat(this, "translationY", 0f, 10f,0f, -10f,0f);
        animator.setDuration(2000); // 设置动画持续时间
        //animator.setRepeatMode(ValueAnimator.REVERSE); // 设置动画重复模式为反向
        animator.setRepeatCount(ValueAnimator.INFINITE); // 设置动画重复次数为无限
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    public void startAnimation() {
        if (animator != null && !animator.isRunning()) {
            animator.start();
        }
    }

    public void stopAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }
}