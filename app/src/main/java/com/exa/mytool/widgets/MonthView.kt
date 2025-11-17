package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.aam.mida.base.R
import com.aam.mida.mida_yk.entity.DayInfoEntity
import com.aam.mida.mida_yk.observer.AppbarStateObserable

/**
 *
 * @Description 月份View
 * @Author zechao.zhang
 * @CreateTime 2024/10/28
 */
class MonthView: View {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(
        context, attributeSet, defStyleAttr) {

//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR)
//        // 注意Calendar.MONTH从0开始，需要加1
//        val month = calendar.get(Calendar.MONTH) + 1
//        dataList.addAll(LunarCalendarUtil.getMonthLunarInfo(year, month))

        // 标题数据
        titleList.addAll(resources.getStringArray(com.aam.mida.mida_yk.R.array.month_view_title))
    }

    private val titleList = mutableListOf<String>()

    private val dataList = mutableListOf<DayInfoEntity>()

    /**
     * item横向间距
     */
    private val horizontalSpace = resources.getDimension(R.dimen.px8).toInt()

    /**
     * item 纵向间距
     */
    private val verticalSpace = resources.getDimension(R.dimen.px12).toInt()

    /**
     * 标题item的宽度
     */
    private val titleItemWidth = resources.getDimension(R.dimen.px48).toInt()

    /**
     * 标题item的高度
     */
    private val titleItemHeight = resources.getDimension(R.dimen.px40).toInt()

    /**
     * 普通项item的宽度
     */
    private val itemWidth = resources.getDimension(R.dimen.px48).toInt()

    /**
     * 普通项item的高度
     */
    private val itemHeight = resources.getDimension(R.dimen.px56).toInt()

    /**
     * 今天的背景圆角
     */
    private val bgRadius = resources.getDimension(R.dimen.px8)

    /**
     * 列数
     */
    private val columnCount = 7

    /**
     * 行数
     */
    private var rowCount = 0

    private val monthDataChangeCallback: (List<DayInfoEntity>) -> Unit = {
        post {
            updateDateList(it)
        }
    }

    /**
     * 标题行的画笔
     */
    private val titlePaint = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.black_level1)
        textSize = resources.getDimension(R.dimen.textSize4)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    /**
     * 普通项（第一行）的画笔
     */
    private val normalFirstTextPaint = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.black_level1)
        textSize = resources.getDimension(R.dimen.textSize7)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    /**
     * 普通项（第二行）的画笔
     */
    private val normalSecondTextPaint = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.black_level2)
        textSize = resources.getDimension(R.dimen.px16)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    /**
     * 其他项（第一行）的画笔
     */
    private val otherFirstTextPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#33333333")
        textSize = resources.getDimension(R.dimen.textSize7)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    /**
     * 其他项（第二行）的画笔
     */
    private val otherSecondTextPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#29333333")
        textSize = resources.getDimension(R.dimen.px16)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    private val todayFirstTextPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#FFFF3F3F")
        textSize = resources.getDimension(R.dimen.textSize7)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    private val todaySecondTextPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#CCFF3F3F")
        textSize = resources.getDimension(R.dimen.px16)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    private val todayBackgroundPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#1AE9412D")
        style = Paint.Style.FILL
    }

    /**
     * 是否显示农历
     */
    var showLunarInfo: Boolean = true

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val preferredWidth = paddingLeft + paddingRight + columnCount * itemWidth +
            (columnCount - 1) * horizontalSpace
        val preferredHeight = paddingTop + paddingBottom + titleItemHeight + verticalSpace +
            rowCount * itemHeight + (rowCount - 1) * verticalSpace

        val resolvedWidth = resolveSize(preferredWidth, widthMeasureSpec)
        val resolvedHeight = resolveSize(preferredHeight, heightMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas) {
        drawTitleLine(canvas)
        drawItem(canvas)
    }

    private fun drawTitleLine(canvas: Canvas) {
        val halfLineHeight = (titlePaint.ascent() + titlePaint.descent()) / 2
        val halfTitleCellHeight = titleItemHeight / 2
        val rowCenter  = paddingTop + (halfTitleCellHeight - halfLineHeight)
        for ((index, item) in titleList.withIndex()) {
            var columnCenter = paddingLeft + titleItemWidth / 2
            if (index > 0) {
                columnCenter += index * (titleItemWidth + horizontalSpace)
            }

            canvas.drawText(item, columnCenter.toFloat(), rowCenter, titlePaint)
        }
    }

    private fun drawItem(canvas: Canvas) {
        val top = (paddingTop + titleItemHeight + verticalSpace).toFloat()
        val halfItemCellHeight = itemHeight / 2

        val todayHalfFirstTextHeight = (todayFirstTextPaint.ascent() + todayFirstTextPaint.descent()) / 2
        val todayHalfSecondTextHeight = (todaySecondTextPaint.ascent() + todaySecondTextPaint.descent()) / 2

        val normalHalfFirstTextHeight = (normalFirstTextPaint.ascent() + normalFirstTextPaint.descent()) / 2
        val normalHalfSecondTextHeight = (normalSecondTextPaint.ascent() + normalSecondTextPaint.descent()) / 2

        val otherHalfFirstTextHeight = (otherFirstTextPaint.ascent() + otherFirstTextPaint.descent()) / 2
        val otherHalfSecondTextHeight = (otherSecondTextPaint.ascent() + otherSecondTextPaint.descent()) / 2

        for ((index, dayInfoEntity) in dataList.withIndex()) {
            // 列号
            val column = index % columnCount
            // 行号
            val row = index / columnCount

            // 列中心
            var columnCenter = paddingLeft + itemWidth / 2f
            if (column > 0) {
                columnCenter += column * (itemWidth + horizontalSpace)
            }

            // 行中心
            var rowCenter = top + halfItemCellHeight
            if (row > 0) {
                rowCenter += (itemHeight + verticalSpace) * row
            }

            val secondText = dayInfoEntity.solarFestival.takeIf { !it.isNullOrEmpty() } ?: dayInfoEntity.lunarDay
            if (dayInfoEntity.isToday) {
                // 今天的数据
                canvas.drawRoundRect(columnCenter - itemWidth / 2f,
                    rowCenter - itemHeight / 2f,
                    columnCenter + itemWidth / 2f,
                    rowCenter + itemHeight / 2f,
                    bgRadius, bgRadius, todayBackgroundPaint)
                if (showLunarInfo) {
                    val firstTextY = rowCenter - halfItemCellHeight / 2 - todayHalfFirstTextHeight
                    val secondTextY = rowCenter + halfItemCellHeight / 2 - todayHalfSecondTextHeight
                    canvas.drawText(dayInfoEntity.solarDay, columnCenter, firstTextY, todayFirstTextPaint)
                    canvas.drawText(secondText, columnCenter, secondTextY, todaySecondTextPaint)
                } else {
                    canvas.drawText(dayInfoEntity.solarDay, columnCenter, rowCenter - todayHalfFirstTextHeight, todayFirstTextPaint)
                }

            } else if (dayInfoEntity.isMonth.not()) {
                // 不是当月的数据
                if (showLunarInfo) {
                    val firstTextY = rowCenter - halfItemCellHeight / 2 - otherHalfFirstTextHeight
                    val secondTextY = rowCenter + halfItemCellHeight / 2 - otherHalfSecondTextHeight
                    canvas.drawText(dayInfoEntity.solarDay, columnCenter, firstTextY, otherFirstTextPaint)
                    canvas.drawText(secondText, columnCenter, secondTextY, otherSecondTextPaint)
                } else {
                    canvas.drawText(dayInfoEntity.solarDay, columnCenter, rowCenter-otherHalfFirstTextHeight, otherFirstTextPaint)
                }

            } else {
                // 普通数据
                if (showLunarInfo) {
                    val firstTextY = rowCenter - halfItemCellHeight / 2 - normalHalfFirstTextHeight
                    val secondTextY = rowCenter + halfItemCellHeight / 2 - normalHalfSecondTextHeight
                    canvas.drawText(dayInfoEntity.solarDay, columnCenter, firstTextY, normalFirstTextPaint)
                    canvas.drawText(secondText, columnCenter, secondTextY, normalSecondTextPaint)
                } else {
                    canvas.drawText(dayInfoEntity.solarDay, columnCenter, rowCenter-normalHalfFirstTextHeight, normalFirstTextPaint)
                }

            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        scope?.cancel()
//        scope = CoroutineScope(Dispatchers.Main)
//        scope?.launch(Dispatchers.Main) {
//            val list = withContext(Dispatchers.IO) {
//                val calendar = Calendar.getInstance()
//                val year = calendar.get(Calendar.YEAR)
//                // 注意Calendar.MONTH从0开始，需要加1
//                val month = calendar.get(Calendar.MONTH) + 1
//                LunarCalendarUtil.getMonthLunarInfo(year, month)
//            }
//            updateDateList(list)
//        }
        AppbarStateObserable.registerMonthDataChangeObserver(monthDataChangeCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        scope?.cancel()
//        scope = null

        AppbarStateObserable.unregisterMonthDataChangeObserver(monthDataChangeCallback)
    }

    fun updateDateList(dateList: List<DayInfoEntity>) {
        this.dataList.clear()
        this.dataList.addAll(dateList)

        // 计算行数
        rowCount = if (dataList.size % columnCount == 0) {
            dataList.size / columnCount
        } else {
            dataList.size / columnCount + 1
        }

        requestLayout()
    }
}