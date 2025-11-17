package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.nex3z.flowlayout.FlowLayout

class TVFlowLayout: FlowLayout {

    var onFocusSearchListener:OnFocusSearchListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    override fun focusSearch(focused: View?, direction: Int): View? {
        val view = onFocusSearchListener?.onFocusSearch(focused, direction)
        if (view != null) {
            return view
        }

        if (childCount > 0 && focused == getChildAt(childCount - 1) && direction == View.FOCUS_RIGHT) {
            return null
        }
        return super.focusSearch(focused, direction)
    }

    override fun addView(child: View?) {
        if (child != null && child.id == View.NO_ID) {
            child.id = ViewCompat.generateViewId()
            if (childCount > 0) {
                getChildAt(childCount - 1).nextFocusRightId = child.id
                child.nextFocusLeftId = getChildAt(childCount - 1).id
            }
        }
        super.addView(child)
    }

    ///////////////////////////////////////////////////////////////////////////
    // 内部类
    ///////////////////////////////////////////////////////////////////////////

    interface OnFocusSearchListener {

        /**
         * Returns the view where focus should be requested given the current focused view and
         * the direction of focus search.
         */
        fun onFocusSearch(focused: View?, direction: Int): View?
    }
}