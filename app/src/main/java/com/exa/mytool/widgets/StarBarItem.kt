package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import androidx.appcompat.widget.AppCompatImageView
import com.aam.mida.base.widgets.FocusHighlight
import com.aam.mida.base.widgets.MyFocusHighlightHelper
import com.aam.mida.mida_yk.R

class StarBarItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : AppCompatImageView(context, attrs, defStyle), Checkable {

    private var mChecked = false

    private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)

    private var mBrowseItemFocusHighlight: MyFocusHighlightHelper.BrowseItemFocusHighlight

    private var mFocus = false

    init {
        val zoomIndex = FocusHighlight.ZOOM_FACTOR_XLARGE
        mBrowseItemFocusHighlight = MyFocusHighlightHelper.BrowseItemFocusHighlight(zoomIndex, false)
        isDuplicateParentStateEnabled = true
        setImageResource(R.drawable.ic_star)
        scaleType = ScaleType.FIT_CENTER
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }

    fun onFocusChange(isFocus: Boolean) {
        mFocus = isFocus
        if (isFocus) {
            if (isChecked) {
                mBrowseItemFocusHighlight.onItemFocused(this, true)
            }
        } else {
            mBrowseItemFocusHighlight.onItemFocused(this, false)
        }
    }

    override fun setChecked(checked: Boolean) {
        if (mChecked != checked) {
            mChecked = checked
            refreshDrawableState()
            if (checked) {
                if (mFocus) {
                    mBrowseItemFocusHighlight.onItemFocused(this, true)
                }
            } else {
                mBrowseItemFocusHighlight.onItemFocused(this, false)
            }
        }
    }

    override fun isChecked() = mChecked

    override fun toggle() {
        setChecked(!mChecked)
    }
}