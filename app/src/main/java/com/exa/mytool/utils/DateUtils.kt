package com.exa.mytool.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


/**
 * 时间工具类
 */
object DateUtils {
    /**
     * 传入格式为date
     *
     * @return 1111年11月11日:周1
     */
    fun getDate(date: Date): String {
        //转换成string
        val s = date.toString()
        //截取一下拿到Wed Dec 07
        val substring = s.substring(0, 10)
        //通过空格来分割,
        val split = substring.split(" ").toTypedArray()
        //得到Wed---周几
        var week = split[0]
        //得到月份Dec
        var month = split[1]
        //得到日子07
        val day = split[2]
        //得到年份
        val year = s.substring(s.length - 4, s.length)
        when (week) {
            "Mon" -> week = "周一"
            "Tue" -> week = "周二"
            "Wed" -> week = "周三"
            "Thu" -> week = "周四"
            "Fri" -> week = "周五"
            "Sat" -> week = "周六"
            "Sun" -> week = "周日"
        }
        when (month) {
            "Jan" -> month = "1"
            "Feb" -> month = "2"
            "Mar" -> month = "3"
            "Apr" -> month = "4"
            "May" -> month = "5"
            "Jun" -> month = "6"
            "Jul" -> month = "7"
            "Aug" -> month = "8"
            "Sept" -> month = "9"
            "Oct" -> month = "10"
            "Nov" -> month = "11"
            "Dec" -> month = "12"
        }
        return year + "年" + month + "月" + day + "日:" + week
    }

    /**
     * 传入格式为date
     *
     * @return 1111年11月11日:周1
     */


    /**
     * 返回一个日期2016年11月09日:周一
     *
     * @return
     */
    fun disposeDate(num: Int): String {
        //系统提供的日历类
        val calendar = Calendar.getInstance()
        //num是里当前日期的天数,传0代表今天,1代表明天,-1代表昨天
        calendar.add(Calendar.DAY_OF_YEAR, num)
        //拿到时间,但是格式是:Wed Dec 07 12:10:38 GMT+08:00 2016
        val date = calendar.time
        return getDate(date)
    }

    /**
     * 传入一个2016-3-30
     * n:在日期上增加天数,返回2016年3月30日:周六
     *
     * @param n 要增加的天数
     * @return 增加之后的天数
     */
    fun disposeDate(dd: String?, n: Int): String {
        //先将String日期转换成Date类型
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = null
        try {
            date = dateFormat.parse(dd)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val cal = Calendar.getInstance()
        //最重要的一步,把日期设置给日历
        cal.time = date
        //n是你要增加的天数,如果要算前几天的就传负数
        cal.add(Calendar.DATE, n)
        val time = cal.time
        return getDate(time)
    }


    /**
     *
     * @param num ﹣3 三天前 +3 三天后
     * @return
     */
    fun getDayAgoOrAfterString(num: Int): String {
        // 时间表示格式可以改变，yyyyMMdd需要写例如20160523这种形式的时间
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()

        val calendar = Calendar.getInstance()
        calendar.time = date
        // add方法中的第二个参数n中，正数表示该日期后n天，负数表示该日期的前n天
        calendar.add(Calendar.DATE, num)
        val date1 = calendar.time
        return sdf.format(date1)
    }

    /**
     *
     * @param num ﹣3 三天前 +3 三天后
     * @return
     */
    fun getDayAgoOrAfterString2(num: Int): String {
        // 时间表示格式可以改变，yyyyMMdd需要写例如20160523这种形式的时间
        val sdf = SimpleDateFormat("MM-dd")
        val date = Date()

        val calendar = Calendar.getInstance()
        calendar.time = date
        // add方法中的第二个参数n中，正数表示该日期后n天，负数表示该日期的前n天
        calendar.add(Calendar.DATE, num)
        val date1 = calendar.time
        return sdf.format(date1)
    }
}
