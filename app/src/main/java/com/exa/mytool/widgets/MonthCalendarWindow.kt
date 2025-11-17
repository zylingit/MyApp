package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.aam.mida.base.GlobalVariable
import com.aam.mida.mida_yk.R
import com.aam.mida.mida_yk.YKApplication
import com.aam.mida.mida_yk.databinding.WindowMonthCalendarBinding
import com.aam.mida.mida_yk.utils.LunarCalendarUtil
import com.aam.soundsetting.ProductType
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

/**
 *
 * @Description 月份布局弹窗
 * @Author zechao.zhang
 * @CreateTime 2024/10/28
 */
class MonthCalendarWindow(var context: Context?): PopupWindow() {

    private val mDataBinding: WindowMonthCalendarBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.window_month_calendar,
            null, false)

    private val scope = MainScope()

    init {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT

        contentView = mDataBinding.root

        isOutsideTouchable = true
    }

    override fun showAsDropDown(anchor: View) {
        super.showAsDropDown(anchor, -abs(anchor.measuredWidth - (context?.resources?.getDimension(R.dimen.px420)?.toInt()?: 0)),
            context?.resources?.getDimension(R.dimen.px8)?.toInt()?: 0)

        if (GlobalVariable.isEnglishLanguage(YKApplication.app)) {
            //2242 2704 英文版，不需要农历
            if (GlobalVariable.produceType == ProductType.HS68_2242
                || GlobalVariable.produceType == ProductType.HS68_2704
            ) {
                mDataBinding.monthView.showLunarInfo = false
                mDataBinding.monthView.invalidate()
            }
        }

        scope.launch {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 注意Calendar.MONTH从0开始，需要加1

            val dateTxt = LunarCalendarUtil.getCalendarAndLunarInfo(mDataBinding.monthView.showLunarInfo)
            mDataBinding.tvDateTxt.text = dateTxt
//            mDataBinding.tvMonthTxt.text = YKApplication.app.getString(R.string.str_sys_month,month.toString())
            mDataBinding.tvMonthTxt.text = setCalendarMonth(month)
        }
    }

    private fun setCalendarMonth(month: Int): String {
        val mouthStr = when (month) {
            1 -> {
                YKApplication.app.getString(R.string.string_calendar_jan)
            }

            2 -> {
                YKApplication.app.getString(R.string.string_calendar_feb)
            }

            3 -> {
                YKApplication.app.getString(R.string.string_calendar_mar)
            }

            4 -> {
                YKApplication.app.getString(R.string.string_calendar_apr)
            }

            5 -> {
                YKApplication.app.getString(R.string.string_calendar_may)
            }

            6 -> {
                YKApplication.app.getString(R.string.string_calendar_jun)
            }

            7 -> {
                YKApplication.app.getString(R.string.string_calendar_jul)
            }

            8 -> {
                YKApplication.app.getString(R.string.string_calendar_aug)
            }

            9 -> {
                YKApplication.app.getString(R.string.string_calendar_sep)
            }

            10 -> {
                YKApplication.app.getString(R.string.string_calendar_oct)
            }

            11 -> {
                YKApplication.app.getString(R.string.string_calendar_nov)
            }

            12 -> {
                YKApplication.app.getString(R.string.string_calendar_dec)
            }
            else->{
                YKApplication.app.getString(R.string.string_calendar_jan)
            }
        }
        return mouthStr
    }




    override fun dismiss() {
        super.dismiss()
        context = null
        mDataBinding.unbind()
        scope.cancel()
    }
}