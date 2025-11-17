package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * @author          pgl
 * @time            2022/4/11 16:09
 * @des             wifi扫描动画View
 *
 * @version         $
 * @updateAuthor    $
 * @updateDate      $
 * @updateDes       wifi扫描动画View
 */
class WifiScaningView : View {
    private val startAngle = -135f //开始角度
    private val sweepAngle = 90f //弧旋转角度
    private val signCount = 4 //信号大小
    private var sholdExisSignalSize = 0 //每次应该绘制的信号个数
    private var signalRadius = 0f
    private val rectf = RectF()
    private var radius = 0f

    /**
     * 自定义控件是否脱离窗体
     */
    private var isDetached = false
    private var mPaint: Paint = Paint().apply {
        color = Color.rgb(0x3E, 0x53, 0x67)//3E5367
        strokeWidth = 6f
        isAntiAlias = true
    }

    private var wifiLenght: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        wifiLenght = w.coerceAtMost(h)
    }

    override fun onDraw(canvas: Canvas) {
        sholdExisSignalSize++
        if (sholdExisSignalSize > signCount) {
            sholdExisSignalSize = 1
        }
        canvas.save()
        signalRadius = wifiLenght / 2f / signCount
        canvas.translate(0f, signalRadius)
        for (i in 0..signCount) {
            if (i >= signCount - sholdExisSignalSize) {
                //定义每个信号所在圆的半径
                radius = signalRadius * i
                rectf.left = radius
                rectf.top = radius
                rectf.right = wifiLenght - radius
                rectf.bottom = wifiLenght - radius
                if (i < signCount - 1) {
                    mPaint.style = Paint.Style.STROKE
                    canvas.drawArc(rectf, startAngle, sweepAngle, false, mPaint)
                } else {
                    mPaint.style = Paint.Style.FILL
                    canvas.drawArc(rectf, startAngle, sweepAngle, true, mPaint)
                }
            }
        }
        canvas.restore()
//        LogUtils.d("pgl","onDraw------------")
        if (isDetached.not()) {
            Thread.sleep(500L)
            invalidate()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isDetached = true
    }
}