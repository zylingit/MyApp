package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.aam.mida.mida_yk.R

/**
 * 星星条
 */
class StarBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    var score = 1
        private set

    init {
        orientation = HORIZONTAL
        focusable = View.FOCUSABLE
        gravity = Gravity.CENTER_VERTICAL

        val px80 = context.resources.getDimension(R.dimen.px80).toInt()
        val px64 = context.resources.getDimension(R.dimen.px64).toInt()
        val px48 = context.resources.getDimension(R.dimen.px48).toInt()
        for (index in 0 until 5) {
            val container = RelativeLayout(context, attrs, defStyle)
            container.focusable = View.NOT_FOCUSABLE
            container.isDuplicateParentStateEnabled = true
            container.setOnClickListener { updateScore(index + 1) }
            val starItem = StarBarItem(context, attrs, defStyle)
            starItem.isSelected = index < score
            container.addView(starItem, RelativeLayout.LayoutParams(px64, px64).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            })
            addView(container, LayoutParams(px80, px80).apply {
                    if (index > 0) {
                        leftMargin = px48
                    }
            })
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            // +
            if (score != 5) {
                updateScore(score + 1)
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            // -
            if (score != 1) {
                updateScore(score - 1)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun updateScore(newScore: Int) {
        score = newScore.coerceIn(1, 5)
        children.forEachIndexed { index, view ->
            ((view as? ViewGroup)?.getChildAt(0) as? StarBarItem)?.isChecked = index < score
        }
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        children.forEachIndexed { index, view ->
            ((view as? ViewGroup)?.getChildAt(0) as? StarBarItem)?.onFocusChange(gainFocus)
        }
    }
}