package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * 弧形进度条
 */
class CircularProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var progress = 0f
    private val progressStrokeWidth = 20f  // 彩色进度条的宽度设置为 20f
    private val backgroundStrokeWidth = progressStrokeWidth * 2 / 3  // 背景进度条宽度

    // 默认灰色
    private val defaultColor = Color.parseColor("#D3D3D3") // 灰色

    // 渐变色数组 (红色 -> 蓝色 -> 黄色 -> 橙色)
    private val colors = intArrayOf(
        Color.parseColor("#FF6700"),  // 红色
        Color.parseColor("#2A80FA"),  // 蓝色
        Color.parseColor("#FFCB46"),  // 黄色
        Color.parseColor("#FF6700")   // 橙色
    )

    // 颜色位置数组，控制颜色过渡的位置
    private val colorPositions = floatArrayOf(
        0.1f,   // 红色起始点
        0.43f,  // 蓝色起始点
        0.76f,  // 黄色起始点
        1.1f    // 橙色起始点
    )

    private val rect = RectF()
    private val circlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val halfProgressStroke = progressStrokeWidth / 2
        val offset = (progressStrokeWidth - backgroundStrokeWidth) / 2

        // 绘制彩色进度条的矩形边界
        rect.set(halfProgressStroke, halfProgressStroke, width - halfProgressStroke, height - halfProgressStroke)

        // 绘制灰色背景环，位置居中
        paint.strokeWidth = backgroundStrokeWidth
        paint.shader = null  // 清除渐变效果
        paint.color = defaultColor
        canvas.drawArc(rect, 135f, 270f, false, paint)  // 绘制完整的灰色弧

        // 绘制彩色进度条
        paint.strokeWidth = progressStrokeWidth
        val sweepGradient = SweepGradient(width / 2f, height / 2f, colors, colorPositions)
        paint.shader = sweepGradient
        canvas.drawArc(rect, 135f, progress, false, paint)

        // 绘制进度圆点
        drawProgressDot(canvas)
    }

    private fun drawProgressDot(canvas: Canvas) {
        // 计算进度圆点的角度
        val angle = Math.toRadians((135 + progress).toDouble())

        // 计算圆点的位置
        val radius = (width / 2f) - (progressStrokeWidth / 2f)
        val cx = (width / 2f + radius * Math.cos(angle)).toFloat()
        val cy = (height / 2f + radius * Math.sin(angle)).toFloat()

        // 绘制与进度条同宽的白色圆点
        canvas.drawCircle(cx, cy, progressStrokeWidth / 2f, circlePaint)
    }

    // 设置进度方法 (0 到 100)
    fun setProgress(progress: Float) {
        this.progress = 2.7f * progress  // 将 0-100% 转换为度数
        invalidate()
    }
}