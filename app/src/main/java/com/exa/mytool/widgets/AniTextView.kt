package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.aam.mida.base.utils.AniUtils

class AniTextView : AppCompatTextView, View.OnTouchListener {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    init {
        setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> AniUtils.scale(this, 1.0f, 0.95f)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                AniUtils.scale(this, 0.95f, 1.0f)
            else -> {
            }
        }
        return false
    }


}