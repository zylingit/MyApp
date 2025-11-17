package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 解决RecyclerView同方向滑动冲突，子RecyclerView
 */
public class ChildRecyclerview extends RecyclerView {
    public ChildRecyclerview(@NonNull Context context) {
        super(context);
    }

    public ChildRecyclerview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChildRecyclerview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //通知父层ViewGroup不要拦截点击事件
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

}