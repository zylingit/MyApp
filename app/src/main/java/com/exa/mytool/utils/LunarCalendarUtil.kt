package com.aam.mida.mida_yk.utils

import cn.hutool.core.date.ChineseDate
import cn.hutool.core.date.DateUtil
import cn.hutool.core.date.chinese.SolarTerms
import com.aam.mida.mida_yk.entity.DayInfoEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

object LunarCalendarUtil {
    fun getMonthLunarInfo(calendar: Calendar, year: Int, month: Int): MutableList<DayInfoEntity> {
//        val monthInfo = mutableListOf<List<DayInfoEntity>>()
        val monthInfo = mutableListOf<DayInfoEntity>()
//        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)

        // 找到当前月份的第一天是星期几
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 上个月的天数
        calendar.add(Calendar.MONTH, -1)
        val daysInLastMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.MONTH, 1)  // 还原到当前月份

        // 添加上个月的日期
        val previousMonthDays = (daysInLastMonth - firstDayOfWeek + 2..daysInLastMonth).map { day ->
            calendar.set(year, month - 2, day)
            createDayInfo(calendar.time,false)
        }

        // 添加当前月的日期
        val currentMonthDays = (1..daysInMonth).map { day ->
            calendar.set(year, month - 1, day)
            createDayInfo(calendar.time,true)
        }

        // 添加下个月的日期
//        calendar.add(Calendar.MONTH, 1)
//        val nextMonthDays = (1..42 - (previousMonthDays.size + currentMonthDays.size)).map { day ->
//            calendar.set(year, month, day)
//            createDayInfo(calendar.time,false)
//        }
        // 当月最后一天是星期几
        val lastDateOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val nextMonthDayCount = 7 - lastDateOfWeek
        val nextMonthDays = mutableListOf<DayInfoEntity>()
        for (index in 0 until nextMonthDayCount) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            nextMonthDays.add(createDayInfo(calendar.time,false))
        }

//        Calendar.SUNDAY

//        // 合并日期
//        val allDays = previousMonthDays + currentMonthDays + nextMonthDays
//
//        // 每七天分一组
//        allDays.chunked(7).forEach {
//            monthInfo.add(it)
//        }

        monthInfo.addAll(previousMonthDays)
        monthInfo.addAll(currentMonthDays)
        monthInfo.addAll(nextMonthDays)

        return monthInfo
    }

    fun createDayInfo(date: Date,isMonth:Boolean): DayInfoEntity {
        val solarDate = DateUtil.format(date, "dd") // 只显示日
        val lunarDate = ChineseDate(date)
        val lunarDay = lunarDate.chineseDay // 只显示农历日
        val lunarFestival = lunarDate.festivals
        val solarFestival = SolarTerms.getTerm(date)
        // 检查日期是否是今天
        val isToday = DateUtil.isSameDay(date, Date())
        return DayInfoEntity(solarDate, lunarDay, lunarFestival, solarFestival,isMonth,isToday)
    }

    fun getCalendarAndLunarInfo(showLunarInfo: Boolean): String {
        // 获取当前日期
        val calendar = Calendar.getInstance()
        val date = calendar.time

        // 获取公历日期
        val solarDate = DateUtil.format(date, "yyyy-MM-dd")

        // 获取农历日期
        val lunarDate = ChineseDate(date)
        val lunarMonth = lunarDate.chineseMonth
        val lunarDay = lunarDate.chineseDay

        return if (showLunarInfo) "$solarDate $lunarMonth$lunarDay" else solarDate
    }

    fun getCurrentTimeUsingLocalTime(): String {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return now.format(formatter)
    }

}