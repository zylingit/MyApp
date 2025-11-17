package com.aam.mida.mida_yk.utils

import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.mida.base.base.appContext
import com.aam.mida.base.utils.WifiUtils
import com.aam.mida.mida_yk.entity.Aw5808RfEntity
import com.aam.mida.mida_yk.observer.AppbarStateObserable
import com.aam.soundsetting.ProductType
import com.aam.soundsetting.VDSocket
import com.am.websocket.utils.MessageUtil
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeoutException
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author          pgl
 * @time            2023/1/3 16:07
 * @des             检查麦克风连接状态
 *
 * @version         $
 * @updateAuthor    $
 * @updateDate      $
 * @updateDes
 */
class CheckThread : Thread() {
    private val TAG = CheckThread::class.java.simpleName
    private var lastTotalRxBytes: Long = 0
    private var lastTimeStamp: Long = 0
    private val calendar = Calendar.getInstance()

    /**
     * 记录当前的年月日，用于判断日期是否发生变化
     */
    private var currentDate: Triple<Int, Int, Int>? = null

    companion object {
        @Volatile
        var checkFlag = true
        var preWifiSignal = -1
    }

    /**
     * 获取wifi信号资源
     * @param maxLevel 设备wifi最大信号强度
     * @param level 当前wifi信号强度
     * */
    private fun getWifiSignalResourceId(maxLevel: Int, level: Int): Int {
//        LogUtils.d(TAG, "getWifiSignalResourceId level = $level")
        var singalLevel = level
        if (level < 0) {
            singalLevel = 0
        }

        var index = (singalLevel / maxLevel.toFloat() * 3f).roundToInt()
        if (index >= 3) {
            index = 3
        } else if (index < 0) {
            index = 0
        }
//        LogUtils.d(TAG, "getWifiSignalResourceId index = $index")
        return index
    }

    /**
     * 获取信号强度，范围是-1000到0，信号好：-50到0，信号中等：-70到-50，信号差：小于-70
     * */
    private fun getWifiLevelResourceId(level: Int, maxLevel: Int = 1000): Int {
        val signal = if (level == -1001) {
            1000
        } else {
            abs(level)
        }
        return when (signal) {
            in 0 until 50 -> {
                0
            }
            in 50 until 70 -> {
                1
            }
            in 70 until 100 -> {
                2
            }
            in 100 until 200 -> {
                3
            }
            else -> {
                4
            }
        }
    }


    private val mRfStatusCallback = object : VDSocket.MessageListener {
        override fun onSuccess(data: String) {
            LogUtils.d(TAG, "mRfStatusCallback send result: $data")
            val entity =  MessageUtil.convert(data, Aw5808RfEntity::class.java)
            AppbarStateObserable.onHeadsetStatusChanged(entity.connected)
        }

        override fun onError(e: java.lang.Exception?) {
            try {
                LogUtils.e(TAG, "mRfStatusCallback send error $e")
            } catch (e: TimeoutException) {
            }
        }
    }

    /**
     * 获取蓝牙耳机连接状态
     * */
    private fun reqAw5808RfStatus() {
//        AAMAudioSDK.getInstance().getAw5808trxRfStatus(mRfStatusCallback)
    }

    private fun showNetSpeed(): Pair<String, String> {
        val nowTotalRxBytes: Long = getTotalRxBytes()
        val nowTimeStamp = System.currentTimeMillis()
        //毫秒转换
        val speed: Long =
            ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp)).coerceAtLeast(0)
        //毫秒转换
        val speed2: Long =
            ((nowTotalRxBytes - lastTotalRxBytes) * 1000 % (nowTimeStamp - lastTimeStamp)).coerceAtLeast(0)
        lastTimeStamp = nowTimeStamp
        lastTotalRxBytes = nowTotalRxBytes
        val valueF = "$speed.$speed2".toFloat() * 8
        val networkSpeed = if (valueF > 1024 * 1024) {
            Pair(String.format("%.1f", valueF / 1024 / 1024), "Gb/s")
        } else if (valueF > 1024) {
            Pair(String.format("%.1f", valueF / 1024), "Mb/s")
        } else if (valueF < 100) {
            Pair(String.format("%.1f", valueF), "Kb/s")
        } else {
            Pair("${valueF.toInt()}", "Kb/s")
        }
        return networkSpeed
    }

    private fun getTotalRxBytes(): Long {
        if (GlobalVariable.produceType == ProductType.HS68_20611) {
            return getNetworkByte() / 1024
        } else {
//            return if (TrafficStats.getUidRxBytes(appContext.applicationInfo.uid) == TrafficStats.UNSUPPORTED.toLong()) 0 else TrafficStats.getTotalRxBytes() / 1024 //转为KB
            var netInterface = getDefaultNetworkInterface2()
            if (netInterface == null) {
                netInterface = if (WifiUtils.getNetworkType() == 0) {
                    "wlan0"
                } else {
                    "eth0"
                }
            }

            return getNetworkByte(netInterface) / 1024
        }
    }

    /**
     *  获取时间、WIFI状态
     * */
    override fun run() {
        var wifiSigna: Int
        while (checkFlag) {
//            reqAw5808RfStatus()

            val rssi = WifiUtils.getInstance(appContext).rssi
            wifiSigna = if (rssi == -1001) {
                //未连接
                -1
            } else {
//                getWifiSignalResourceId(1000, abs(rssi))
                getWifiLevelResourceId(rssi, 1000)
            }
            val networkSpeed = if (WifiUtils.getNetworkType() == 0 && rssi == -1001) {
                // wlan 未连接
                Pair("0", "Kb/s")
            } else {
                showNetSpeed()
            }
            AppbarStateObserable.onNetworkSpeedChanged(networkSpeed.first, networkSpeed.second)

            // LogUtils.v(TAG, "preWifiSignal = $preWifiSignal,wifiSignalId = $wifiSignalId")
            if (WifiUtils.getNetworkType() == 0) {
                // 在wlan的情况下才刷新
                if (preWifiSignal != wifiSigna) {
                    preWifiSignal = wifiSigna
                    AppbarStateObserable.onWifiSignalChanged(wifiSigna)
                }
            }


            AppbarStateObserable.onDateTimeChanged()

            // 更新日历中的日期
            updateCalendarDate()

            try {
                sleep(5000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 更新日历中的日期
     */
    private fun updateCalendarDate() {
        // 由于使用了单例，故在此要重置为当前时间
        calendar.time = Date()
        // 获取月份数据
        val year = calendar.get(Calendar.YEAR)
        // 注意Calendar.MONTH从0开始，需要加1
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        if (currentDate == null
            || currentDate?.first != year
            || currentDate?.second != month
            || currentDate?.third != day) {
            currentDate = Triple(year, month, day)
            AppbarStateObserable.onMonthDataChange(LunarCalendarUtil.getMonthLunarInfo(calendar, year, month))
        }
    }
}