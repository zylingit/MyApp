package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.HorizontalScrollView

class AutoScrollView: HorizontalScrollView {


    private var handler: Handler? = null

    private var runnable: Runnable? = null


    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        init()
    }

    private fun init() {
        handler = Handler(Looper.myLooper()!!)

        var looperCount = 0

        runnable = Runnable {
            val sx = scrollX
            if (sx + width < getChildAt(0).width){
                scrollTo(sx + 2, 0)
                handler?.postDelayed(runnable!!, 32)
            }else{
                scrollTo(0, 0)
                if (looperCount < 2) {
                    handler?.postDelayed(runnable!!, 2_000)
                    looperCount++
                }
            }
        }
    }

    fun startScrolling(){
        runnable?.let {
            handler?.post(it)
        }
    }

    fun stopScrolling(){
        runnable?.let {
            handler?.removeCallbacks(it)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopScrolling()
    }
}