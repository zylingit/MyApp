package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.aam.mida.mida_yk.R

class EmptyView: FrameLayout {


    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)



    init {
//        LayoutInflater.from(context).inflate(R.layout.empty_layout, this, true)
        val iconIv = AppCompatImageView(context)
        iconIv.setImageResource(R.mipmap.icon_empty)
        addView(iconIv, LayoutParams(resources.getDimension(R.dimen.px180).toInt(),
            resources.getDimension(R.dimen.px180).toInt()).apply {
                gravity = Gravity.CENTER_HORIZONTAL
        })

        val textView = AppCompatTextView(context)
        textView.text = context.getString(R.string.movie_search_not_yet_content)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.px32))
        textView.setTextColor(Color.WHITE)
        addView(textView, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        })
    }
}