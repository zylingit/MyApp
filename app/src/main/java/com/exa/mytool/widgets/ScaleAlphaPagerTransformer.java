package com.aam.mida.mida_yk.widgets;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

public class ScaleAlphaPagerTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.6f; //缩放因子
    private static final float MIN_ALPHA = 0.6f;

    @Override
    public void transformPage(View view, float position) {

        if (position >= -1 || position <= 1) {
            final float height = view.getHeight();
            final float width = view.getWidth();
            final float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            final float vertMargin = height * (1 - scaleFactor) / 2;
            final float horzMargin = width * (1 - scaleFactor) / 2;
            view.setPivotY(0.5f * height); //设置缩放的中心点为view的中心，所以不需要设置setPageMargin()了
            view.setPivotX(0.5f * width);
            if (position < 0) {
                view.setTranslationX(horzMargin );
            } else {
                view.setTranslationX(-horzMargin );
            }
            view.setScaleX(scaleFactor); //缩放
            view.setScaleY(scaleFactor);
            //我不需要透明度，所以屏蔽了
            view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

//            GradientDrawable g = new GradientDrawable();
//            g.setColor(Color.argb(1-(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)), 0, 0, 0));
//            g.setCornerRadius(16f);
//            View chV = ((ViewGroup)view).getChildAt(((ViewGroup)view).getChildCount() - 1);
//            if (chV != null) {
//                chV.setForeground(g);
//            }
        }
    }
}
