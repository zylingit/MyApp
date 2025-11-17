package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class SingleTouchRecyclerView: RecyclerView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {}


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        ev?: return super.dispatchTouchEvent(ev)

        if (ev.pointerCount > 1) { // 如果是多点触控则不处理任何操作
            return true // 返回true阻止事件进一步传播（可选）
        }

        return super.dispatchTouchEvent(ev)
    }

}