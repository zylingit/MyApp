package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.aam.mida.base.widgets.ScaleConstraintLayout
import com.aam.mida.mida_yk.R
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView

class KTVRecItemView: ScaleConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr)

    private val px320 = context.resources.getDimension(R.dimen.px320).toInt()
    private val px250 = context.resources.getDimension(R.dimen.px250).toInt()
    private val px10 = context.resources.getDimension(R.dimen.px10).toInt()
    private val px16 = context.resources.getDimension(R.dimen.px16)

    val subRootView: ConstraintLayout

    val selectorImageview: AppCompatImageView

    val songCover: SimpleDraweeView

    val gradient: View

    val songSinger: AppCompatTextView

    val songName: AppCompatTextView

    val songRankImg: AppCompatTextView

    val videoFormat: AppCompatTextView

    val iconScore: AppCompatImageView

    val animView: AppCompatImageView

    init {
        subRootView = ConstraintLayout(context).apply {
            id = View.generateViewId()
            setPadding(px10, px10, px10, px10)
            isDuplicateParentStateEnabled = true
        }

        addView(subRootView)
        ConstraintSet().apply {
            clone(this)
            constrainWidth(subRootView.id, px320)
            constrainHeight(subRootView.id, px250)
            connect(subRootView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(subRootView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(subRootView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(subRootView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }.applyTo(this)

        selectorImageview = AppCompatImageView(context).apply {
            id = View.generateViewId()
            background = context.getDrawable(R.drawable.bg_ktv_recommend_item_ra_16)
        }
        subRootView.addView(selectorImageview)

        songCover = SimpleDraweeView(context).apply {
            id = View.generateViewId()
            hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_XY
            hierarchy.setPlaceholderImage(R.drawable.ktv_default_bg)
            hierarchy.roundingParams = RoundingParams.fromCornersRadius(px16)
        }
        subRootView.addView(songCover)

        gradient = View(context).apply {
            id = View.generateViewId()
            visibility = View.GONE
            background = context.getDrawable(R.drawable.selector_item_bg_gradul)
        }
        subRootView.addView(gradient)

        songSinger = AppCompatTextView(context).apply {
            id = View.generateViewId()
            maxWidth = context.resources.getDimension(R.dimen.px100).toInt()
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.END
            marqueeRepeatLimit = -1
            isSingleLine = true
            setTextColor(context.getColor(R.color.light_level1))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.px18))
        }
        subRootView.addView(songSinger)

        songName = AppCompatTextView(context).apply {
            id = View.generateViewId()
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.START
            marqueeRepeatLimit = -1
            isSingleLine = true
            setTextColor(context.getColor(R.color.light_level1))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.px22))
        }
        subRootView.addView(songName)

        songRankImg = AppCompatTextView(context).apply {
            id = View.generateViewId()
            gravity = Gravity.CENTER
            setTextColor(context.getColor(R.color.light_level1))
            setTypeface(typeface, Typeface.BOLD)
            setTypeface(typeface, Typeface.ITALIC)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.px26))
        }
        subRootView.addView(songRankImg)

        videoFormat = AppCompatTextView(context).apply {
            id = View.generateViewId()
            val px2 = context.resources.getDimension(R.dimen.px2).toInt()
            setPadding(px2, px2, px2, px2)
            background = context.getDrawable(R.drawable.selector_item_song_format_bg)
            gravity = Gravity.CENTER
            setTextColor(context.getColor(R.color.light_level1))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.px18))
        }
        subRootView.addView(videoFormat)

        iconScore = AppCompatImageView(context).apply {
            id = View.generateViewId()
            background = context.getDrawable(R.mipmap.item_scop)
        }
        subRootView.addView(iconScore)

        animView = AppCompatImageView(context).apply {
            id = View.generateViewId()
        }
        subRootView.addView(animView)

        ConstraintSet().apply {
            clone(subRootView)
            constrainWidth(selectorImageview.id, 0)
            constrainHeight(selectorImageview.id, 0)
            connect(selectorImageview.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(selectorImageview.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(selectorImageview.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(selectorImageview.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

            constrainWidth(songCover.id, 0)
            constrainHeight(songCover.id, context.resources.getDimension(R.dimen.px168).toInt())
            connect(songCover.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(songCover.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(songCover.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            constrainWidth(gradient.id, 0)
            constrainHeight(gradient.id, 0)
            connect(gradient.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(gradient.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(gradient.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(gradient.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

            constrainWidth(songSinger.id, ConstraintSet.WRAP_CONTENT)
            constrainHeight(songSinger.id, ConstraintSet.WRAP_CONTENT)
            connect(songSinger.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END,
                context.resources.getDimension(R.dimen.px8).toInt())
            connect(songSinger.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(songSinger.id, ConstraintSet.TOP, songCover.id, ConstraintSet.BOTTOM)

            constrainWidth(songName.id, 0)
            constrainHeight(songName.id, ConstraintSet.WRAP_CONTENT)
            connect(songName.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START,
                context.resources.getDimension(R.dimen.px5).toInt())
            connect(songName.id, ConstraintSet.END, songSinger.id, ConstraintSet.START,
                context.resources.getDimension(R.dimen.px5).toInt())
            connect(songName.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(songName.id, ConstraintSet.TOP, songCover.id, ConstraintSet.BOTTOM)

            constrainWidth(songRankImg.id, 0)
            constrainHeight(songRankImg.id, 0)
            connect(songRankImg.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(songRankImg.id, ConstraintSet.TOP, songName.id, ConstraintSet.TOP)
            connect(songRankImg.id, ConstraintSet.BOTTOM, songName.id, ConstraintSet.BOTTOM,
                context.resources.getDimension(R.dimen.px13).toInt())

            constrainWidth(videoFormat.id, ConstraintSet.WRAP_CONTENT)
            constrainHeight(videoFormat.id, ConstraintSet.WRAP_CONTENT)
            connect(videoFormat.id, ConstraintSet.START, songCover.id, ConstraintSet.START,
                context.resources.getDimension(R.dimen.px16).toInt())
            connect(videoFormat.id, ConstraintSet.BOTTOM, songCover.id, ConstraintSet.BOTTOM,
                context.resources.getDimension(R.dimen.px16).toInt())

            constrainWidth(iconScore.id, context.resources.getDimension(R.dimen.px40).toInt())
            constrainHeight(iconScore.id, context.resources.getDimension(R.dimen.px32).toInt())
            connect(iconScore.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(iconScore.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            constrainWidth(animView.id, context.resources.getDimension(R.dimen.px300).toInt())
            constrainHeight(animView.id, context.resources.getDimension(R.dimen.px168).toInt())
            connect(animView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(animView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(animView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        }.applyTo(subRootView)
    }
}