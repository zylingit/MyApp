package com.aam.mida.mida_yk.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.aam.mida.mida_yk.R

class ScrollBar : FrameLayout {

    private val mScrollBar: View
    private var mRecycleView: RecyclerView? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_scroll_bar, this, true)
        mScrollBar = findViewById(R.id.scrollbar_thumb)
    }

    fun setRecycleView(recycleView: RecyclerView) {
        mRecycleView = recycleView
    }



    fun scroll(position: Int, total: Int, dy:Int) {

        if (total <= 0) {
            return
        }

        val pos = if (dy>0){
            if (position > total - 6) {
                total
            }else{
                position
            }
        }else if (dy<0){
            if (position < 6) {
                0
            }else{
                position
            }
        }else{
            position
        }

        val percent = pos * 1.0f / total
        val transMaxRange = measuredHeight - mScrollBar.measuredHeight
        val range = transMaxRange * percent
        //mScrollBar.translationY = range
//        Log.d("Scroll", "pos=$pos  total=$total  percent $percent   range $range  y${mScrollBar.translationY}")
        val valueAnim = ValueAnimator.ofFloat(mScrollBar.translationY, range)
        valueAnim.duration = 500
        valueAnim.interpolator = LinearInterpolator()
        valueAnim.addUpdateListener {
            val value = it.animatedValue as Float
            mScrollBar.translationY = value
        }
        valueAnim.start()
    }
}