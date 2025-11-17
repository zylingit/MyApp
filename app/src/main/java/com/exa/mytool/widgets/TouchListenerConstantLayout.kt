package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

class TouchListenerConstantLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var touchListener: ((MotionEvent) -> Unit)? = null

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        touchListener?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }
}