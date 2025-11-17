/**
 * MIT License
 *
 * Copyright (c) 2016 yanbo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.aam.mida.mida_yk.R;


/**
 * like SlidingPaneLayout, all direction support.
 */
public class SlideLayout extends ViewGroup {
    public static final int STATE_CLOSE = 0;
    public static final int STATE_SLIDING = 1;
    public static final int STATE_OPEN = 2;

    private static final int SLIDE_RIGHT = 0;
    private static final int SLIDE_LEFT = 1;
    private static final int SLIDE_TOP = 2;
    private static final int SLIDE_BOTTOM = 3;

    private View mContentView;
    private View mSlideView;

    private Scroller mScroller;

    private int mLastX = 0;
    private int mLastY = 0;

    private int mSlideCriticalValue = 0;
    private boolean mIsScrolling = false;
    private int mSlideDirection;


    private Boolean canSlide = true;

    @Nullable
    private HasFocusChangeListener hasFocusChangeListener;
    private boolean selfHasFocus = false;

    private SlideStateChangeListener slideStateChangeListener;

    public void setSlideStateChangeListener(SlideStateChangeListener slideStateChangeListener) {
        this.slideStateChangeListener = slideStateChangeListener;
    }

    public void setCanSlide(Boolean canSlide) {
        this.canSlide = canSlide;
    }
    private final ViewTreeObserver.OnGlobalFocusChangeListener globalFocusChangeListener = new ViewTreeObserver.OnGlobalFocusChangeListener() {
        @Override
        public void onGlobalFocusChanged(View view, View view1) {
            if (hasFocus()) {
                if (!selfHasFocus) {
                    selfHasFocus = true;

                    if (hasFocusChangeListener != null) {
                        hasFocusChangeListener.onFocusChange(true);
                    }
                }
            } else {
                if (selfHasFocus) {
                    selfHasFocus = false;

                    if (hasFocusChangeListener != null) {
                        hasFocusChangeListener.onFocusChange(false);
                    }
                }
            }
        }
    };

    public SlideLayout(Context context) {
        this(context, null);
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideLayout);
        mSlideDirection = typedArray.getInt(R.styleable.SlideLayout_slideDirection, SLIDE_RIGHT);
        mSlideCriticalValue = typedArray.getDimensionPixelSize(R.styleable.SlideLayout_slideCriticalValue, 0);
        typedArray.recycle();

        mScroller = new Scroller(context);

        setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //Log.d("SlideLayout", "scrollX:"+scrollX+"  oldScrollX:"+oldScrollX);
                switch (mSlideDirection){
                    case SLIDE_LEFT:

                        break;
                    case SLIDE_RIGHT:
                        if (slideStateChangeListener != null) {
                            if (scrollX == 0) {
                                slideStateChangeListener.slideStateChange(SLIDE_RIGHT, STATE_CLOSE);
                            } else if (scrollX == mSlideView.getMeasuredWidth()) {
                                slideStateChangeListener.slideStateChange(SLIDE_RIGHT, STATE_OPEN);
                            }
                        }
                        break;
                    case SLIDE_TOP:

                        break;
                    case SLIDE_BOTTOM:

                        break;
                }
            }
        });
    }

    public int getSlideState() {
        int retValue = STATE_CLOSE;
        if (mIsScrolling) {
            retValue = STATE_SLIDING;
        } else {
            int scrollOffset = (mSlideDirection == SLIDE_LEFT || mSlideDirection == SLIDE_RIGHT) ?
                                getScrollX() : getScrollY();
            retValue = (scrollOffset == 0) ? STATE_CLOSE : STATE_OPEN;
        }
        return retValue;
    }

    public void smoothCloseSlide() {
        smoothScrollTo(0, 0);
    }

    public void smoothOpenSlide() {
        switch (mSlideDirection) {
            case SLIDE_RIGHT:
                smoothScrollTo(mSlideView.getMeasuredWidth(), 0);
                break;
            case SLIDE_LEFT:
                smoothScrollTo(-mSlideView.getMeasuredWidth(), 0);
                break;
            case SLIDE_TOP:
                smoothScrollTo(0, -mSlideView.getMeasuredHeight());
                break;
            case SLIDE_BOTTOM:
                smoothScrollTo(0, mSlideView.getMeasuredHeight());
                break;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("SlideLayout only need contains two child (content and slide).");
        }

        mContentView = getChildAt(0);
        mSlideView = getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mContentView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        switch (mSlideDirection) {
            case SLIDE_LEFT:
                mSlideView.layout(-mSlideView.getMeasuredWidth(), 0, 0, getMeasuredHeight());
                break;
            case SLIDE_RIGHT:
                mSlideView.layout(getMeasuredWidth(), 0,
                        mSlideView.getMeasuredWidth() + getMeasuredWidth(), getMeasuredHeight());
                break;
            case SLIDE_TOP:
                mSlideView.layout(0, -mSlideView.getMeasuredHeight(), getMeasuredWidth(), 0);
                break;
            case SLIDE_BOTTOM:
                mSlideView.layout(0, getMeasuredHeight(),
                        getMeasuredWidth(), mSlideView.getMeasuredHeight() + getMeasuredHeight());
                break;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mIsScrolling || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (event.getPointerCount() > 1) {
            return false; // 或者使用 return super.dispatchTouchEvent(event); 根据需求决定是否传递给父类处理
        }

        if (!canSlide){
            return  super.dispatchTouchEvent(event);
        }

        if (isPointInsideView((int) event.getRawX(), (int) event.getRawY(), mSlideView)) {
            //Log.d("SlideLayout", "Point is in sideView");
            if (event.getAction() == MotionEvent.ACTION_UP) {
                smoothCloseSlide();
            }
            return super.dispatchTouchEvent(event);
        }

        int eventX = (int) event.getX();
        int eventY = (int) event.getY();
//        Log.d("SlideLayout", "eventX:"+eventX+ " eventY:"+eventX);
//        Log.d("SlideLayout", "getRawX:"+event.getRawX()+ " getRawY:"+event.getRawY());
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (slideStateChangeListener != null){
                    if (slideStateChangeListener.interceptCurrentTouch()){
                        if (isPointInsideView((int) event.getRawX(), (int) event.getRawY(), mSlideView)) {
//                            Log.d("SlideLayout", "Point is in sideView");
                            super.dispatchTouchEvent(event);
                        }
                        return true;
                    }
                }
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                mIsScrolling = false;
                //Maybe child not set OnClickListener, so ACTION_DOWN need to return true and use super.
                super.dispatchTouchEvent(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                int offsetX = eventX - mLastX;
                int offsetY = eventY - mLastY;
                int directionMoveOffset = 0;
                if (mSlideDirection == SLIDE_LEFT || mSlideDirection == SLIDE_RIGHT) {
                    directionMoveOffset = Math.abs(offsetX) - Math.abs(offsetY);
                } else {
                    directionMoveOffset = Math.abs(offsetY) - Math.abs(offsetX);
                }
                if (!mIsScrolling && directionMoveOffset < ViewConfiguration.getTouchSlop()) {
                    break;
                }
                if (getParent() != null){
                    requestDisallowInterceptTouchEvent(true);
                }
                mIsScrolling = true;
                int newScrollX = 0;
                int newScrollY = 0;
                switch (mSlideDirection) {
                    case SLIDE_RIGHT:
                        newScrollX = scrollX - offsetX;
                        if (newScrollX < 0) {
                            newScrollX = 0;
                        } else if (newScrollX > mSlideView.getMeasuredWidth()) {
                            newScrollX = mSlideView.getMeasuredWidth();
                        }
                        break;
                    case SLIDE_LEFT:
                        newScrollX = scrollX - offsetX;
                        if (newScrollX < -mSlideView.getMeasuredWidth()) {
                            newScrollX = -mSlideView.getMeasuredWidth();
                        } else if (newScrollX > 0) {
                            newScrollX = 0;
                        }
                        break;
                    case SLIDE_TOP:
                        newScrollY = scrollY - offsetY;
                        if (newScrollY < -mSlideView.getMeasuredHeight()) {
                            newScrollY = -mSlideView.getMeasuredHeight();
                        } else if (newScrollY > 0) {
                            newScrollY = 0;
                        }
                        break;
                    case SLIDE_BOTTOM:
                        newScrollY = scrollY - offsetY;
                        if (newScrollY < 0) {
                            newScrollY = 0;
                        } else if (newScrollY > mSlideView.getMeasuredHeight()) {
                            newScrollY = mSlideView.getMeasuredHeight();
                        }
                        break;
                }
                scrollTo(newScrollX, newScrollY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsScrolling = false;
                if (getParent() != null){
                    requestDisallowInterceptTouchEvent(false);
                }
                int finalScrollX = 0;
                int finalScrollY = 0;
                switch (mSlideDirection) {
                    case SLIDE_RIGHT:
                        if (scrollX > getSlideCriticalValue()) {
                            finalScrollX = mSlideView.getMeasuredWidth();
                        }
                        break;
                    case SLIDE_LEFT:
                        if (scrollX < -getSlideCriticalValue()) {
                            finalScrollX = -mSlideView.getMeasuredWidth();
                        }
                        break;
                    case SLIDE_TOP:
                        if (scrollY < -getSlideCriticalValue()) {
                            finalScrollY = -mSlideView.getMeasuredHeight();
                        }
                        break;
                    case SLIDE_BOTTOM:
                        if (scrollY > getSlideCriticalValue()) {
                            finalScrollY = mSlideView.getMeasuredHeight();
                        }
                        break;
                }
                smoothScrollTo(finalScrollX, finalScrollY);
                break;
        }

        mLastX = eventX;
        mLastY = eventY;
        return super.dispatchTouchEvent(event);
    }

    //TODO  when mSlideCriticalValue != 0, slide critical need fix.
    private int getSlideCriticalValue() {
        if (mSlideDirection == SLIDE_LEFT || mSlideDirection == SLIDE_RIGHT) {
            if (mSlideCriticalValue == 0) {
                mSlideCriticalValue = mSlideView.getMeasuredWidth() / 2;
            }
        } else {
            if (mSlideCriticalValue == 0) {
                mSlideCriticalValue = mSlideView.getMeasuredHeight() / 2;
            }
        }
        return mSlideCriticalValue;
    }

    private void smoothScrollTo(int destX, int destY) {
        int scrollX = getScrollX();
        int deltaX = destX - scrollX;
        int scrollY = getScrollY();
        int deltaY = destY - scrollY;
        mScroller.startScroll(scrollX, scrollY, deltaX, deltaY,
                (int) (Math.abs(Math.sqrt(deltaX*deltaX + deltaY*deltaY)) * 3));
        postInvalidate();
    }

    private boolean isPointInsideView(int x, int y, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        return (x >= viewX && x <= (viewX + view.getWidth())) &&
                (y >= viewY && y <= (viewY + view.getHeight()));
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalFocusChangeListener(globalFocusChangeListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalFocusChangeListener(globalFocusChangeListener);
    }

    public void setHasFocusChangeListener(@Nullable HasFocusChangeListener hasFocusChangeListener) {
        this.hasFocusChangeListener = hasFocusChangeListener;
    }

    public interface HasFocusChangeListener {

        void onFocusChange(boolean hasFocus);
    }

    public interface SlideStateChangeListener{
        void slideStateChange(int direction, int slideState);
        boolean interceptCurrentTouch();
    }
}
