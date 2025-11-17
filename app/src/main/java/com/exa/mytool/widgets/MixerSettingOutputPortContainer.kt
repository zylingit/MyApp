package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children

/**
 *
 * @Description
 * @Author zechao.zhang
 * @CreateTime 2024/09/29
 */
class MixerSettingOutputPortContainer: LinearLayoutCompat {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    override fun addFocusables(views: ArrayList<View>, direction: Int, focusableMode: Int) {
        if (hasFocus()) {
            super.addFocusables(views, direction, focusableMode)
        } else {
            children.forEach {
                if (it.isSelected) {
                    views.add(it)
                }
            }
        }
    }
}