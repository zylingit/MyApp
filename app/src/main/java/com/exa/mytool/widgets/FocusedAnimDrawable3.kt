package com.aam.mida.mida_yk.widgets

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.animation.LinearInterpolator
import com.aam.mida.base.base.appContext
import com.aam.mida.mida_yk.R
import java.lang.Math.*

/**
 *Created by lzb on 2024年10月7日
 *Description：平行四边形，角度为八十
 */
class FocusedAnimDrawable3: Drawable() {

    private val TAG = "AnimDrawable"

    private val path = Path()
    private val destPath1 = Path()
    private val destPath2 = Path()

    /**
     * 默认光圈层线段宽度
     */
    private val lineWidth = 2f

    /**
     * 最大透明度值
     */
    private val maxAlpha = 255

    private var baseColor = Color.WHITE

    private val paint = Paint().apply {
        color = baseColor
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        isAntiAlias = true
    }

    /**
     * 用于做颜色过渡动画
     */
    private val alphaRange = Pair(0x33, 0xFF)

    /**
     * 光圈层初始颜色
     */
    private val wholePaintOriginalColor = Color.argb(alphaRange.first, Color.red(baseColor),
        Color.green(baseColor), Color.blue(baseColor))

    /**
     * 光圈层绘制相关
     */
    private val wholePaint = Paint().apply {
        color = wholePaintOriginalColor
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        isAntiAlias = true
    }

    /**
     * 当前环绕次数
     */
    private var roundCount = 0

    /**
     * 最大环绕次数，当超过该次数时，光圈层会开始渐变成不透明白色
     */
    private val maxRepeatCount = 3

    private val pathMeasure = PathMeasure()

    /**
     * 线段长度
     */
    private var lineLen = 0

    /**
     * 周长
     */
    private var totalLen = 0f

    private val colorList = mutableListOf<Int>()
    private val colorPositionList = mutableListOf<Float>()

    private val mtx = Matrix()
    private var degree: Float = 0f

    /**
     * 圆角半径
     */
    var radius: Float = 0f

    /**
     * 线条小线段长度
     */
    private val DEFAULT_SEGMENT_LENGHT = 1.6f

    /**
     * 环绕一圈的时间
     */
    private val roundingDuration = 5000L

    /**
     * 环绕动画
     */
    private val roundingAnimation = ObjectAnimator.ofFloat( 0f, 360f).apply {
        duration = roundingDuration
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
//            animValue = (it.animatedValue as Int) * 1.0f / 100
//            val start = System.currentTimeMillis()
//            prepareData()
//            Log.d(TAG, "spend: ${System.currentTimeMillis() - start}")

            degree = it.animatedValue as Float
            invalidateSelf()
        }

        addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
//                Log.d(TAG, "onAnimationStart")
            }

            override fun onAnimationEnd(animation: Animator?) {
//                Log.d(TAG, "onAnimationEnd")
            }

            override fun onAnimationCancel(animation: Animator?) {
//                Log.d(TAG, "onAnimationCancel")
            }

            override fun onAnimationRepeat(animation: Animator?) {
                roundCount += 1

                if (roundCount == maxRepeatCount) {
//                    wholePaint.color = Color.convert(1f, 1f, 1f, ColorSpace.get(ColorSpace.Named.SRGB))
//                    Log.d("AnimDrawable", "onAnimationRepeat: start")
                    colorChangeAnim.start()
                } else if (roundCount == maxRepeatCount + 1) {
                    roundCount = -1
//                    wholePaint.color = wholePaintOriginalColor
//                    Log.d("AnimDrawable", "onAnimationRepeat: reverse")
                    colorChangeAnim.reverse()
                }
            }

        })
    }
    private val colorChangeAnim = ValueAnimator.ofInt(alphaRange.first, alphaRange.second).apply {
        duration = roundingDuration * 3 / 4
        interpolator = LinearInterpolator()
        repeatCount = 0
        addUpdateListener {
            val alpha = it.animatedValue as Int
            wholePaint.color = Color.argb(alpha, Color.red(baseColor),
                Color.green(baseColor), Color.blue(baseColor))
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        path.reset()

        // 获取中心坐标
        val centerX = (left + right) / 2f
        val centerY = (top + bottom) / 2f

        // 定义平行四边形的宽度和高度
        val width = right - left
        val height = bottom - top

        // 设置平行四边形的偏移量
        val offset = 15f // 控制平行四边形的斜度
        val leftShift = 8f // 控制整体向左的偏移量

        // 定义圆角半径
        val cornerRadius = 30f // 调整这个值来改变圆角大小

        // 四个顶点的坐标 (加入 leftShift 实现整体左移)
        val topLeftX = centerX - width / 2f + offset - leftShift
        val topLeftY = centerY - height / 2f
        val topRightX = centerX + width / 2f + offset - leftShift
        val topRightY = centerY - height / 2f
        val bottomRightX = centerX + width / 2f - leftShift
        val bottomRightY = centerY + height / 2f
        val bottomLeftX = centerX - width / 2f - leftShift
        val bottomLeftY = centerY + height / 2f

        // 使用圆角画出平行四边形
        path.moveTo(topLeftX + cornerRadius, topLeftY)
        path.lineTo(topRightX - cornerRadius, topRightY)
        path.quadTo(topRightX, topRightY, topRightX, topRightY + cornerRadius)

        path.lineTo(bottomRightX, bottomRightY - cornerRadius)
        path.quadTo(bottomRightX, bottomRightY, bottomRightX - cornerRadius, bottomRightY)

        path.lineTo(bottomLeftX + cornerRadius, bottomLeftY)
        path.quadTo(bottomLeftX, bottomLeftY, bottomLeftX, bottomLeftY - cornerRadius)

        path.lineTo(topLeftX, topLeftY + cornerRadius)
        path.quadTo(topLeftX, topLeftY, topLeftX + cornerRadius, topLeftY)

        path.close()

        pathMeasure.setPath(path, true)
        totalLen = pathMeasure.length

        lineLen = min(abs(left - right), abs(top - bottom))

        initColorList()

        paint.shader = SweepGradient(bounds.centerX().toFloat(), bounds.centerY().toFloat(),
            colorList.toIntArray(),
            colorPositionList.toFloatArray())

    }

    private fun initColorList() {
        var segmentSize = (lineLen * 1.0f / DEFAULT_SEGMENT_LENGHT).toInt()
        if (segmentSize == 0) {
            return
        }

        colorList.clear()

        val tmpColorList = mutableListOf<Int>()
        val centerStart = (segmentSize * 0.4).toInt()
        val centerEnd = (segmentSize * 0.6).toInt()

        val perAlpha = (maxAlpha / (segmentSize * 0.4)).toInt()
        for (index in 0 until segmentSize) {
            val alpha = if (index < centerStart) {
                min(perAlpha * index, maxAlpha)
            } else if (index > centerEnd){
//                255 - perAlpha * (index - centerEnd)
                min(perAlpha * (segmentSize - index), maxAlpha)
            } else {
                min(perAlpha * centerStart, maxAlpha)
            }

            tmpColorList.add(Color.argb(alpha, Color.red(baseColor), Color.green(baseColor),
                Color.blue(baseColor)))
        }

        // 第一段线段
        colorList.addAll(tmpColorList)
        // 过渡区
        colorList.add(Color.argb(0, Color.red(baseColor), Color.green(baseColor),
            Color.blue(baseColor)))

        // 第二段线段
        colorList.addAll(tmpColorList)
        // 过渡区
        colorList.add(Color.argb(0, Color.red(baseColor), Color.green(baseColor),
            Color.blue(baseColor)))

        colorPositionList.clear()

        // 每一个小线段占总长度的比例
        val perPercent = DEFAULT_SEGMENT_LENGHT / totalLen

        //第一段线段的位置
        for (index in 1 .. tmpColorList.size) {
            colorPositionList.add(perPercent * index)
        }
        // 过渡区
        colorPositionList.add(0.5f)

        //第二段线段的位置
        for (index in 1 .. tmpColorList.size) {
            colorPositionList.add(perPercent * index + 0.5f)
        }
        // 过渡区
        colorPositionList.add(1f)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, wholePaint)

        mtx.reset()
        mtx.setRotate(degree, bounds.centerX().toFloat(), bounds.centerY().toFloat())
        (paint.shader as SweepGradient).setLocalMatrix(mtx)
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun setTint(tintColor: Int) {
        super.setTint(tintColor)
        baseColor = tintColor
        paint.color = tintColor
        wholePaint.color = Color.argb(alphaRange.first, Color.red(baseColor),
            Color.green(baseColor), Color.blue(baseColor))
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun unscheduleSelf(what: Runnable) {
        super.unscheduleSelf(what)

//        Log.d(TAG, "unscheduleSelf: animation end")
    }

    fun start() {
        roundingAnimation.start()
//        Log.d(TAG, "animation start")
    }

    fun stop() {
        colorChangeAnim.end()
        roundingAnimation.end()
        reset()
//        Log.d(TAG, "animation end")
    }

    private fun reset() {
        roundCount = -1
        wholePaint.color = wholePaintOriginalColor
        destPath1.reset()
        destPath2.reset()

    }
}