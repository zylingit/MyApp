package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.aam.mida.mida_yk.R

/**
 *
 * @Description 灯光示意图
 * @Author zechao.zhang
 * @CreateTime 2024/01/25
 */
class LightView: View {

    companion object {
        /**
         * 宽边上的球的数量
         */
        private const val WIDTH_COUNT = 48

        /**
         * 高边上的球的数量
         */
        private const val HEIGHT_COUNT = 27

        /**
         * 半径
         */
        private const val BOLL_RADIUS = 6f

        /**
         * 球的间隔
         */
        private const val BOOL_INTERVAL = 7f
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    private val bgBitmap: Bitmap

    private val srcRect: Rect

    private var desRectF: RectF = RectF()

    private var leftPercent = 1f
    private var topPercent = 1f
    private var rightPercent = 1f
    private var bottomPercent = 1f

    /**
     * 是否是顺时针
     */
    var isClockwise = true

    init {
        bgBitmap = BitmapFactory.decodeResource(resources, R.mipmap.bg_light_view)
        srcRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
    }

    private val defaultColor = Color.parseColor("#FF46245F")

    private val defaultPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 顶边的画笔（除去两角）
     */
    private val topPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 右边的画笔（除去两角）
     */
    private val rightPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 底边的画笔（除去两角）
     */
    private val bottomPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 左边的画笔（除去两角）
     */
    private val leftPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 左上角画笔
     */
    private val leftTopPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 右上角画笔
     */
    private val rightTopPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 右下角画笔
     */
    private val rightBottomPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 左下角画笔
     */
    private val leftBottomPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 用于画白球
     */
    private val whitePaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 用于画灰色球
     */
    private val grayPaint = Paint().apply {
        color = defaultColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = WIDTH_COUNT * BOLL_RADIUS * 2 + BOOL_INTERVAL * (WIDTH_COUNT - 1)
        val height = HEIGHT_COUNT * BOLL_RADIUS * 2 + BOOL_INTERVAL * (HEIGHT_COUNT - 1)

        desRectF.left = BOLL_RADIUS * 3 + BOOL_INTERVAL
        desRectF.top = BOLL_RADIUS * 2 + BOOL_INTERVAL / 2
        desRectF.right = width - BOLL_RADIUS * 3 - BOOL_INTERVAL
        desRectF.bottom =  height - BOLL_RADIUS * 2 - BOOL_INTERVAL / 2
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // 画四个角
        // 左上角
        canvas?.drawCircle(BOLL_RADIUS , BOLL_RADIUS , BOLL_RADIUS, leftTopPaint)
        // 右上角
        canvas?.drawCircle(measuredWidth - BOLL_RADIUS , BOLL_RADIUS , BOLL_RADIUS, rightTopPaint)
        // 右下角
        canvas?.drawCircle(measuredWidth - BOLL_RADIUS , measuredHeight - BOLL_RADIUS , BOLL_RADIUS, rightBottomPaint)
        // 左下角
        canvas?.drawCircle(BOLL_RADIUS , measuredHeight - BOLL_RADIUS , BOLL_RADIUS, leftBottomPaint)

        if (isClockwise) {
            // 顺时针

            // 顶边，除去角的球
            var startX = BOLL_RADIUS * 2 + BOOL_INTERVAL
            // 顶部亮灯数量
            val topColorCount = ((WIDTH_COUNT - 2) * topPercent).toInt()
            for (index in 0 until WIDTH_COUNT - 2) {
                val paint = if (index <= topColorCount) {
                    topPaint
                } else {
                    defaultPaint
                }

                canvas?.drawCircle(startX + BOLL_RADIUS , BOLL_RADIUS , BOLL_RADIUS, paint)
                startX += BOLL_RADIUS * 2 + BOOL_INTERVAL
            }

            // 底边，除去角的球
            startX = BOLL_RADIUS * 2 + BOOL_INTERVAL
            // 底部亮灯数量
            val bottomColorCount = ((WIDTH_COUNT - 2) * bottomPercent).toInt()
            for (index in 0 until WIDTH_COUNT - 2) {
                val paint = if (index <= bottomColorCount) {
                    bottomPaint
                } else {
                    defaultPaint
                }
                canvas?.drawCircle(startX + BOLL_RADIUS , measuredHeight - BOLL_RADIUS , BOLL_RADIUS, paint)
                startX += BOLL_RADIUS * 2 + BOOL_INTERVAL
            }

            // 左边，去除角的球
            var startY = BOLL_RADIUS * 2 + BOOL_INTERVAL
            // 左部亮灯数量
            val leftColorCount = ((WIDTH_COUNT - 2) * leftPercent).toInt()
            for (index in 0 until HEIGHT_COUNT - 2) {
                val paint = if (index <= leftColorCount) {
                    leftPaint
                } else {
                    defaultPaint
                }
                canvas?.drawCircle(BOLL_RADIUS, startY + BOLL_RADIUS, BOLL_RADIUS, paint)
                startY += BOLL_RADIUS * 2 + BOOL_INTERVAL
            }

            // 右边，去除角的球
            // 右部亮灯数量
            val rightColorCount = ((WIDTH_COUNT - 2) * rightPercent).toInt()
            startY = BOLL_RADIUS * 2 + BOOL_INTERVAL
            for (index in 0 until HEIGHT_COUNT - 2) {
                val paint = if (index <= rightColorCount) {
                    rightPaint
                } else {
                    defaultPaint
                }
                canvas?.drawCircle(measuredWidth - BOLL_RADIUS, startY + BOLL_RADIUS, BOLL_RADIUS, paint)
                startY += BOLL_RADIUS * 2 + BOOL_INTERVAL
            }
        } else {
            // 逆时针

            // 顶边，除去角的球
            var startX = measuredWidth - (BOLL_RADIUS * 2 + BOOL_INTERVAL)
            // 顶部亮灯数量
            val topColorCount = ((WIDTH_COUNT - 2) * topPercent).toInt()
            for (index in 0 until WIDTH_COUNT - 2) {
                val paint = if (index <= topColorCount) {
                    topPaint
                } else {
                    defaultPaint
                }

                canvas?.drawCircle(startX + BOLL_RADIUS , BOLL_RADIUS , BOLL_RADIUS, paint)
                startX -= BOLL_RADIUS * 2 + BOOL_INTERVAL
            }

            // 底边，除去角的球
            startX = measuredWidth - (BOLL_RADIUS * 2 + BOOL_INTERVAL)
            // 底部亮灯数量
            val bottomColorCount = ((WIDTH_COUNT - 2) * bottomPercent).toInt()
            for (index in 0 until WIDTH_COUNT - 2) {
                val paint = if (index <= bottomColorCount) {
                    bottomPaint
                } else {
                    defaultPaint
                }
                canvas?.drawCircle(startX + BOLL_RADIUS , measuredHeight - BOLL_RADIUS , BOLL_RADIUS, paint)
                startX -= BOLL_RADIUS * 2 + BOOL_INTERVAL
            }

            // 左边，去除角的球
            var startY = measuredHeight - (BOLL_RADIUS * 2 + BOOL_INTERVAL)
            // 左部亮灯数量
            val leftColorCount = ((WIDTH_COUNT - 2) * leftPercent).toInt()
            for (index in 0 until HEIGHT_COUNT - 2) {
                val paint = if (index <= leftColorCount) {
                    leftPaint
                } else {
                    defaultPaint
                }
                canvas?.drawCircle(BOLL_RADIUS, startY + BOLL_RADIUS, BOLL_RADIUS, paint)
                startY -= BOLL_RADIUS * 2 + BOOL_INTERVAL
            }

            // 右边，去除角的球
            // 右部亮灯数量
            val rightColorCount = ((WIDTH_COUNT - 2) * rightPercent).toInt()
            startY = measuredHeight - (BOLL_RADIUS * 2 + BOOL_INTERVAL)
            for (index in 0 until HEIGHT_COUNT - 2) {
                val paint = if (index <= rightColorCount) {
                    rightPaint
                } else {
                    defaultPaint
                }
                canvas?.drawCircle(measuredWidth - BOLL_RADIUS, startY + BOLL_RADIUS, BOLL_RADIUS, paint)
                startY -= BOLL_RADIUS * 2 + BOOL_INTERVAL
            }
        }


        // 绘制背景图片
        canvas?.drawBitmap(bgBitmap, srcRect, desRectF, whitePaint)
    }

    fun reset() {
        leftTopPaint.color = defaultColor
        rightTopPaint.color = defaultColor
        rightBottomPaint.color = defaultColor
        leftBottomPaint.color = defaultColor

        leftPaint.color = defaultColor
        rightPaint.color = defaultColor
        topPaint.color = defaultColor
        bottomPaint.color = defaultColor

        invalidate()
    }

    fun setLeftTopColor(color: Int) {
        leftTopPaint.color = color
        invalidate()
    }

    fun setRightTopColor(color: Int) {
        rightTopPaint.color = color
        invalidate()
    }

    fun setRightBottomColor(color: Int) {
        rightBottomPaint.color = color
        invalidate()
    }

    fun setLeftBottomColor(color: Int) {
        leftBottomPaint.color = color
        invalidate()
    }

    /**
     * 设置左边的颜色
     * @param color 颜色值
     * @param percent 染色的比例 [0, 1]
     */
    fun setLeftColor(color: Int, percent: Float) {
        leftPercent = if (percent < 0) {
            0f
        } else if (percent > 1) {
            1f
        } else {
            percent
        }

        leftPaint.color = color
        invalidate()
    }

    /**
     * 设置顶边的颜色
     * @param color 颜色值
     * @param percent 染色的比例 [0, 1]
     */
    fun setTopColor(color: Int, percent: Float) {
        topPercent = if (percent < 0) {
            0f
        } else if (percent > 1) {
            1f
        } else {
            percent
        }

        topPaint.color = color
        invalidate()
    }

    /**
     * 设置右边的颜色
     * @param color 颜色值
     * @param percent 染色的比例 [0, 1]
     */
    fun setRightColor(color: Int, percent: Float) {
        rightPercent = if (percent < 0) {
            0f
        } else if (percent > 1) {
            1f
        } else {
            percent
        }

        rightPaint.color = color
        invalidate()
    }

    /**
     * 设置底边的颜色
     * @param color 颜色值
     * @param percent 染色的比例 [0, 1]
     */
    fun setBottomColor(color: Int, percent: Float) {
        bottomPercent = if (percent < 0) {
            0f
        } else if (percent > 1) {
            1f
        } else {
            percent
        }

        bottomPaint.color = color
        invalidate()
    }
}