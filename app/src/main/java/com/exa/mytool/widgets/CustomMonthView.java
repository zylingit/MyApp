package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.graphics.Canvas;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MultiMonthView;

public class CustomMonthView extends MultiMonthView {
    private int mRadius;

    public CustomMonthView(Context context) {
        super(context);
    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelectedPre, boolean isSelectedNext) {
        //绘制选中的背景
        int cx = x + mItemWidth / 2;
        int cy = y + mItemHeight / 3 + 10;

        mSelectedPaint.setAntiAlias(true);
        canvas.drawCircle(cx, cy, mRadius, mSelectedPaint);
        return true;
    }

    @Override
    protected void onPreviewHook() {
        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y, boolean isSelected) {

    }

    /**
     * 绘制文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        int top = y - mItemHeight / 8;

        if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                    isSelected ? mSelectTextPaint
                            : calendar.isCurrentDay() ? mCurDayLunarTextPaint
                            : calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);

        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                    isSelected ? mSelectTextPaint
                            : calendar.isCurrentDay() ? mCurDayLunarTextPaint
                            : calendar.isCurrentMonth() ? mCurMonthTextPaint : mOtherMonthTextPaint);
        }
//        if (!isSelected && calendar.isCurrentDay())
//            canvas.drawText("今", cx, mTextBaseLine + top, mCurDayTextPaint);

    }
}