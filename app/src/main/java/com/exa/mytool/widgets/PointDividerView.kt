package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.aam.mida.mida_yk.R

/**
 * Created by lzy on 2023/10/25.
 * 撸一串圆点做分割线
 */
class PointDividerView : View {
    private var mPaint: Paint? = null// 画笔
    private var radius = 0f // 圆的半径
    private var dividerWidth = 0f // 圆的间距

    private val mColor = Color.parseColor("#d1d1d1") // 圆点的颜色
    private var mContext: Context? = null

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            widthMeasureSpec,
            mContext!!.resources.getDimension(R.dimen.px10).toInt()
        )
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        mContext = context
        mPaint = Paint()
        radius = mContext!!.resources.getDimension(R.dimen.px5)
        dividerWidth = mContext!!.resources.getDimension(R.dimen.px20)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint?.isAntiAlias = true
        mPaint?.color = mColor // 设置颜色
        val measuredHeight = measuredHeight / 2 // 高度居中
        val measuredWidth = measuredWidth
        // int maxCount = measuredWidth / (dividerWidth + radius * 2);
        var i = radius
        while (i < measuredWidth) {
            canvas.drawCircle(i, measuredHeight.toFloat(), radius, mPaint!!) // 小圆
            i += dividerWidth
        }
    }
}