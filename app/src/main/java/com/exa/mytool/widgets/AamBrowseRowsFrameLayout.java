
package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class AamBrowseRowsFrameLayout extends FrameLayout {

    public AamBrowseRowsFrameLayout(Context context) {
        this(context ,null);
    }

    public AamBrowseRowsFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AamBrowseRowsFrameLayout(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void measureChildWithMargins(View child,
            int parentWidthMeasureSpec, int widthUsed,
            int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + widthUsed, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + heightUsed, lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

}
