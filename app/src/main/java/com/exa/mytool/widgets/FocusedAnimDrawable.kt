package com.aam.mida.mida_yk.widgets

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.animation.LinearInterpolator
import java.lang.Math.*

class FocusedAnimDrawable: Drawable() {

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
    private val tmpPathMeasure = PathMeasure()

    /**
     * 线段长度
     */
    private var lineLen = 0

    /**
     * 周长
     */
    private var totalLen = 0f

    private var animValue: Float = 0f

    private val pathSegments1 = mutableListOf<PathSegment>()

    private val pathSegments2 = mutableListOf<PathSegment>()

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
    private val roundingAnimation = ValueAnimator.ofInt(0, 100).apply {
        duration = roundingDuration
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            animValue = (it.animatedValue as Int) * 1.0f / 100
//            val start = System.currentTimeMillis()
            prepareData()
//            Log.d(TAG, "spend: ${System.currentTimeMillis() - start}")
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
        path.addRoundRect(RectF(bounds.left.toFloat() + lineWidth,
            bounds.top.toFloat() + lineWidth,
            bounds.right.toFloat() - lineWidth,
            bounds.bottom.toFloat() - lineWidth), radius, radius, Path.Direction.CW)
        pathMeasure.setPath(path, true)
        totalLen = pathMeasure.length

        lineLen = min(abs(left - right), abs(top - bottom))
    }

    override fun draw(canvas: Canvas) {

        canvas.drawPath(destPath1, paint)
        canvas.drawPath(destPath2, paint)
        pathSegments1.forEach {
            paint.alpha = it.alpha
            paint.strokeWidth = it.width
            canvas.drawPath(it.path, paint)
        }

        pathSegments2.forEach {
            paint.alpha = it.alpha
            paint.strokeWidth = it.width
            canvas.drawPath(it.path, paint)
        }

        canvas.drawPath(path, wholePaint)
    }

    private fun prepareData() {
        destPath1.reset()
        destPath2.reset()
        //        pathSegments1.clear()
        //        pathSegments2.clear()

        val star1 = animValue * totalLen

        if (star1 == totalLen) {
            pathMeasure.getSegment(0f, lineLen * 1f, destPath1, true)
            split(pathSegments1, destPath1)
        } else if (star1 + lineLen > totalLen) {
            pathMeasure.getSegment(star1, totalLen, destPath1, true)
            pathMeasure.getSegment(0f, star1 + lineLen - totalLen, destPath1, false)
            split(pathSegments1, destPath1)
        } else {
            pathMeasure.getSegment(star1, star1 + lineLen, destPath1, true)
            split(pathSegments1, destPath1)
        }

        var star2 = if (animValue > 0.5) {
            ((animValue - 0.5) * totalLen).toFloat()
        } else {
            ((animValue + 0.5) * totalLen).toFloat()
        }

        //        var changeColor2 = false
        // 由于超出长度的部分不会被绘制，故需要补充线段
        if (star2 == totalLen) {
            pathMeasure.getSegment(0f, lineLen * 1f, destPath2, true)
            split(pathSegments2, destPath2)
        } else if (star2 + lineLen > totalLen) {
            pathMeasure.getSegment(star2, totalLen, destPath2, true)
            pathMeasure.getSegment(0f, star2 + lineLen - totalLen, destPath2, false)
            split(pathSegments2, destPath2)
        } else {
            pathMeasure.getSegment(star2, star2 + lineLen, destPath2, true)
            split(pathSegments2, destPath2)
        }
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

    /**
     * 将pah分段，制作成中间粗、两端系的线条
     */
    private fun split(list:  MutableList<PathSegment>, path: Path) {
//        val pm = PathMeasure(path, false)
        tmpPathMeasure.setPath(path, false)
        val length = tmpPathMeasure.length
        var segmentSize = (length * 1.0f / DEFAULT_SEGMENT_LENGHT).toInt()
        if (segmentSize == 0) {
            return
        }

        val isEmptyList = list.isEmpty()

        val centerStart = (segmentSize * 0.4).toInt()
        val centerEnd = (segmentSize * 0.6).toInt()
//        Log.d("AnimDrawable", "range($centerStart,$centerEnd)")

        val minWidth = 0.4f
        val perWidth = abs(lineWidth - minWidth) / (segmentSize * 0.4f)
        val perAlpha = (maxAlpha / (segmentSize * 0.4)).toInt()
        val result = mutableListOf<PathSegment>()
        var lastPosition = 0f
        for (index in 0 until segmentSize) {
            val ps = if (isEmptyList) {
                Path()
            } else {
                val item = list[index]
                item.path.reset()
                item.path
            }

            val start = lastPosition
            val end = min((start + DEFAULT_SEGMENT_LENGHT), length)
            tmpPathMeasure.getSegment(start, end, ps, true)
            lastPosition = end
            val alpha = if (index < centerStart) {
                min(perAlpha * index, maxAlpha)
            } else if (index > centerEnd){
//                255 - perAlpha * (index - centerEnd)
                min(perAlpha * (segmentSize - index), maxAlpha)
            } else {
                min(perAlpha * centerStart, maxAlpha)
            }

            val width = if (index < centerStart) {
                minWidth + perWidth * index
            } else if (index > centerEnd){
//                lineWidth - perWidth * (index - centerEnd)
                minWidth + perWidth * (segmentSize - index)
            } else {
                lineWidth
            }

            if (isEmptyList) {
                result.add(PathSegment(ps, width, alpha))
            }
        }

        if (isEmptyList) {
            list.addAll(result)
        }
    }

    data class PathSegment(var path: Path, var width: Float, var alpha: Int)
}