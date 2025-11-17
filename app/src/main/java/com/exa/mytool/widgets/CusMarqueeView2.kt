package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.aam.mida.mida_yk.R
import kotlinx.coroutines.*

/**
 * Created by 江坚 on 2020/5/7
 * Description：
 */
class CusMarqueeView2(context: Context, attrs: AttributeSet?, defStyle: Int) :
    View(context, attrs, defStyle) {
    /*
     * 1、自定义View的属性
     * 2、在View的构造方法中获得我们自定义的属性
     * 3、重写onMeasure
     * 4、重写onDraw
     */
    /*
     * 文本
     */
    private var mTitleText: String? = ""

    /**
     * 文本的颜色
     */
    private var mTitleTextColor = 0

    /**
     * 文本的大小
     */
    private var mTitleTextSize = 0

    /**
     * 绘制时控制文本绘制的范围
     */
    private lateinit var mBound /*, usualBound*/: Rect

    // 画笔
    private var mPaint: Paint?

    //滚动频率，默认15毫秒一次，数值太小会影响效率
    private var speed = 15

    //默认每次左移3px，数值太大会有停顿感，数值太小滚动会变慢
    private var length = 1
    private var currentLength = 0

    private var job: Job? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0) {
        currentLength = length
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else {
            mPaint?.textSize = mTitleTextSize.toFloat()
            mPaint?.getTextBounds(mTitleText, 0, mTitleText!!.length, mBound)
            val textWidth = mBound.width().toFloat() // 字体宽度

            // 控件padding
            val desired = (paddingLeft + textWidth + paddingRight).toInt()
            width = desired
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            mPaint?.textSize = mTitleTextSize.toFloat()
            mPaint?.getTextBounds(mTitleText, 0, mTitleText!!.length, mBound)
            val textHeight = mBound.height().toFloat()
            val desired = (paddingTop + textHeight + paddingBottom).toInt()
            height = desired
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        if (mBound.width() <= width) {
            if (currentLength < mBound.width()) {
                mPaint?.let {
                    canvas.drawText(
                        mTitleText!!,
                        (paddingLeft - currentLength).toFloat(),
                        (height / 2 + mBound.height() / 2 - 2).toFloat(),
                        it
                    )
                }
            }
            if (currentLength >= 120) {
                mPaint?.let {
                    canvas.drawText(
                        mTitleText!!,
                        (width - currentLength + 120).toFloat(),
                        (height / 2 + mBound.height() / 2 - 2).toFloat(),
                        it
                    )
                }
            }
        } else {
            if (currentLength < mBound.width()) {
                mPaint?.let {
                    canvas.drawText(
                        mTitleText!!,
                        (paddingLeft - currentLength).toFloat(),
                        (height / 2 + mBound.height() / 2 - 2).toFloat(),
                        it
                    )
                }
            }
            if (currentLength >= mBound.width() - width + 120) {
                mPaint?.let {
                    canvas.drawText(
                        mTitleText!!,
                        (120 - currentLength + mBound.width()).toFloat(),
                        (height / 2 + mBound.height() / 2 - 2).toFloat(),
                        it
                    )
                }
            }
        }
    }

    fun startScroll() {
        job?.cancel()
        scroll()
    }

    fun stopScroll() {
        job?.cancel()
        currentLength = 0
        invalidate()
    }

    fun setScrollLength(length: Int) {
        this.length = length
        currentLength = length
    }

    fun setSpeed(speed: Int) {
        this.speed = speed
    }

    fun setText(msg: String?) {
        mTitleText = msg
        mBound = Rect()
        if (mPaint == null) {
            return
        }
        mPaint?.getTextBounds(msg, 0, mTitleText!!.length, mBound)
        invalidate()
    }

    fun onDestroy() {
        stopScroll()
        mPaint = null
    }


    private fun scroll() {
        job = GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                if (mBound.width() <= width) {
                    currentLength = length
                } else {
                    if (currentLength >= mBound.width() + 120) {
                        currentLength = length
                    } else {
                        currentLength += length
                    }
                }
                invalidate()
                delay(speed.toLong())
            }
        }
    }

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CustomTitleView, defStyle, 0)
        val n = a.indexCount
        for (i in 0 until n) {
            when (val attr = a.getIndex(i)) {
                R.styleable.CustomTitleView_titleText -> mTitleText = a.getString(attr)
                R.styleable.CustomTitleView_titleTextColor -> mTitleTextColor =
                    a.getColor(attr, Color.BLACK)
                R.styleable.CustomTitleView_titleTextSize -> mTitleTextSize =
                    a.getDimensionPixelSize(
                        attr,
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            16f,
                            resources.displayMetrics
                        ).toInt()
                    )
            }
        }
        a.recycle()
        mPaint = Paint()
        mPaint?.flags = Paint.ANTI_ALIAS_FLAG
        mPaint?.textSize = mTitleTextSize.toFloat()
        mPaint?.color = mTitleTextColor
        mBound = Rect()
        mPaint?.getTextBounds(mTitleText, 0, mTitleText!!.length, mBound)
    }

    fun setTextColor(color: Int) {
        mTitleTextColor = color
        mPaint?.color = color
        invalidate()
    }
    fun setTextSize(size: Int) {
        mTitleTextSize = size
        mPaint?.textSize = size.toFloat()
        invalidate()
    }
}