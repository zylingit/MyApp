package com.aam.mida.mida_yk.utils

import android.content.Context
import android.net.TrafficStats
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

class NetSpeedUtil {

    private var lastTotalRxBytes: Long = 0
    private var lastTimeStamp: Long = 0

    fun getNetSpeedStr(appContext: Context): String {
        val nowTotalRxBytes: Long = getTotalRxBytes(appContext)
        val nowTimeStamp = System.currentTimeMillis()
        val speed: Long =
            (nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp) //毫秒转换
        val speed2: Long =
            (nowTotalRxBytes - lastTotalRxBytes) * 1000 % (nowTimeStamp - lastTimeStamp) //毫秒转换
        lastTimeStamp = nowTimeStamp
        lastTotalRxBytes = nowTotalRxBytes
        val valueF = "$speed.$speed2".toFloat()
        val networkSpeed = if (valueF > 1024 * 1024) {
            "${String.format("%.1f", valueF / 1024 / 1024)}G/s"
        } else if (valueF > 1024) {
            "${String.format("%.1f", valueF / 1024)}M/s"
        } else if (valueF < 100) {
            "${String.format("%.1f", valueF)}K/s"
        } else {
            "${valueF.toInt()}K/s"
        }
        return networkSpeed
    }

    private fun getTotalRxBytes(appContext: Context): Long {
        return if (TrafficStats.getUidRxBytes(appContext.applicationInfo.uid) == TrafficStats.UNSUPPORTED.toLong()) 0 else TrafficStats.getTotalRxBytes() / 1024 //转为KB
    }

    /**
     * 获取网络类型
     *
     * @return -1: 未连接，0: wlan，1: eth
     */
    fun getNetworkType(): Int {
        try {
            var hasEthIp = false
            var hasWlanIp = false
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
//                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                var hasIp = false
                val enumIpAddr = nif.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        // return inetAddress.getAddress().toString();
                        hasIp = true
                    }
                }
                if (!hasIp) {
                    continue
                }
                val interfaceName = nif.name.uppercase()
                if (interfaceName.contains("WLAN")) {
                    hasWlanIp = true
                } else if (interfaceName.contains("ETH")) {
                    hasEthIp = true
                }
            }
            if (hasEthIp) {
                return 1
            } else if (hasWlanIp) {
                return 0
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return -1
        }
        return -1
    }
}