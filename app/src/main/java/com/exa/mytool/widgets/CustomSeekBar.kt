package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.aam.mida.mida_yk.R
import kotlin.math.roundToInt

/**
 * @author          pgl
 * @time            2023/1/7 11:00
 * @des             当前进度显示在thumb上的seekbar
 *
 * @version         1.0v$
 * @updateAuthor    pgl$
 * @updateDate      $
 * @updateDes       当前进度显示在thumb上的seekbar
 */
class CustomSeekBar : AppCompatSeekBar {
    private val mPaint: Paint = Paint()
    private val mRect: Rect = Rect()
    private var mDisplayOffset = 0
    private var isVerticality = false
    private var isShowAdd = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mPaint.apply {
            color = resources.getColor(R.color.light_level1, null)
            textSize = resources.getDimension(R.dimen.textSize10)
            isAntiAlias = true
        }
    }

    fun setDisplayOffset(displayOffset: Int) {
        mDisplayOffset = displayOffset
    }

    fun isVerticality(boolean: Boolean) {
        isVerticality = boolean
    }

    fun isShowAdd(showAdd: Boolean){
        isShowAdd = showAdd
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val h = MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(
            widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                h,
                MeasureSpec.EXACTLY
            )
        )
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        val rect: Rect = thumb.bounds

        val text = if (isVerticality) {
            if ((progress + mDisplayOffset) > 0) {
                if (isShowAdd) {
                    "+${progress + mDisplayOffset}"
                }else{
                    "${progress + mDisplayOffset}"
                }
            } else {
                (progress + mDisplayOffset).toString()
            }
        } else {
            (progress + mDisplayOffset).toString()
        }
        mPaint.getTextBounds(text, 0, text.length, mRect)
        val x = rect.centerX().toFloat() - mRect.centerX().toFloat()
        val y = if (isVerticality) {
            val y_ = rect.bottom.toFloat() + resources.getDimension(R.dimen.px4)
            canvas.rotate(90f, x, y_)
            y_
        } else {
            rect.bottom.toFloat() + resources.getDimension(R.dimen.px20)
        }
        canvas.drawText(text, x, y, mPaint)
        canvas.restore()
    }
}