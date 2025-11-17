package com.exa.mytool.utils

import android.net.Uri
import android.util.Log
import com.aam.mida.base.net.BaseUrlManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
import java.io.InputStreamReader
import java.io.LineNumberReader
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * 获取当前使用的网卡
 */
suspend fun getDefaultNetworkInterface(): String? = withContext(Dispatchers.IO) {
    val testHost = Uri.parse(BaseUrlManager.MIDA_URL).host ?: "www.baidu.com"
    var result: String? = null
    var remoteAddress: InetAddress? = null
    try {
        remoteAddress = InetAddress.getByName(testHost)
    } catch (e: Exception) {
        Log.d("NetUtil", "unknown host", e)
    }

    if (remoteAddress != null) {
        val s = DatagramSocket()
        try {
            s.connect(remoteAddress, 80)
            result = NetworkInterface.getByInetAddress(s.localAddress)?.name
        } catch (e: Exception) {
          Log.e("NetUtil", "connect error", e)
        } finally {
            s.closeQuietly()
        }
    }
    result
}

/**
 * 获取wlan0和eth0的字节数据
 */
fun getNetworkByte(): Long {
    var returnData: LineNumberReader? = null
    var process: Process? = null
    try {
        process = Runtime.getRuntime().exec("cat /proc/net/dev")
        val r = InputStreamReader(process.inputStream)
        returnData = LineNumberReader(r)
        var totalData: Long = 0
        for (line in returnData.readLines()) {
            val networkData = line.trim()
            var data = networkData.split(" ")
            data = data.filter { it.isNotEmpty() }
            if (data.size >= 2) {
                if (data[0] == "eth0:" || data[0] == "wlan0:") {
                    totalData += data[1].toLong()
                    Log.i("network", "${data[0]} ${data[1]}")
                }

            }
        }
        return totalData

    } catch (e: Exception) {
        e.printStackTrace()
        return 0
    } finally {
        returnData?.closeQuietly()

        if (process?.isAlive == true) {
            process.destroyForcibly()
        }
    }
}

fun getNetworkByte(networkInterface: String): Long {
    var process: Process? = null
    var returnData: LineNumberReader? = null
    try {
        process = Runtime.getRuntime().exec("cat /proc/net/dev")
        val r = InputStreamReader(process.inputStream)
        returnData = LineNumberReader(r)
        var totalData: Long = 0
        for (line in returnData.readLines()) {
            val networkData = line.trim()
            var data = networkData.split(" ")
            data = data.filter { it.isNotEmpty() }
            if (data.size >= 2) {
                if (data[0].startsWith(networkInterface)) {
                    totalData += data[1].toLong()
                    Log.i("network", "${data[0]} ${data[1]}")
                    break
                }

            }
        }
        return totalData

    } catch (e: Exception) {
        e.printStackTrace()
        return 0
    } finally {
        returnData?.closeQuietly()

        if (process?.isAlive == true) {
            process.destroyForcibly()
        }
    }
}

fun getDefaultNetworkInterface2(): String?  {
    val testHost = Uri.parse(BaseUrlManager.MIDA_URL).host ?: "www.baidu.com"
    var result: String? = null
    var remoteAddress: InetAddress? = null
    try {
        remoteAddress = InetAddress.getByName(testHost)
    } catch (e: Exception) {
        Log.d("NetUtil", "unknown host", e)
    }

    if (remoteAddress != null) {
        val s = DatagramSocket()
        try {
            s.connect(remoteAddress, 80)
            result = NetworkInterface.getByInetAddress(s.localAddress)?.name ?:""
        } finally {
            s.closeQuietly()
        }
    }
    return result
}