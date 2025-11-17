package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.aam.mida.common.widgets.ScaleConstraintLayout
import com.aam.mida.mida_yk.R

/**
 *
 * @Description TODO
 * @Author zechao.zhang
 * @CreateTime 2024/01/25
 */
class LightCornerItemView: ConstraintLayout, OnFocusChangeListener {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    private val leftIv: AppCompatImageView
    private val textView: AppCompatTextView

    init {

        LayoutInflater.from(context).inflate(R.layout.item_light_corner, this, true)
        leftIv = findViewById(R.id.leftIv)
        textView = findViewById(R.id.textView)
        background = ContextCompat.getDrawable(context, R.drawable.bg_machine_light_corner)
        onFocusChangeListener = this
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            leftIv.visibility = View.VISIBLE
            textView.setTextColor(Color.parseColor("#222222"))
        } else {
            leftIv.visibility = View.GONE
            textView.setTextColor(Color.WHITE)
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
//        super.onFocusChange(v, hasFocus)
        if (hasFocus) {
            leftIv.visibility = View.VISIBLE
            textView.setTextColor(Color.parseColor("#222222"))
        } else {
            leftIv.visibility = View.GONE
            textView.setTextColor(Color.WHITE)
        }
    }

    fun setText(text: String) {
        textView.text = text
    }
}