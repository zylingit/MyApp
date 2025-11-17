package com.aam.mida.mida_yk.utils

import android.net.Uri
import android.util.Log
import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.mida.base.SystemPropertyConstant
import com.am.websocket.ApiConstant
import com.am.websocket.WSManager
import com.jan.aammedia.PropertiesUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import java.net.URLEncoder

object VideoProxyUtil {
    const val PROP_AAM_PROXY_TYPE = "persist.aam.proxy.type"
    private const val PORT = 10231
    private const val TAG = "VideoProxy"
    private var systemVersion = ""
    private var isProxyValid = false

    /**
     * 判断业务服是否配置了NAS的IP
     */
    var hasNasIp = false
    var NAS_BASE_URL = "http://10.0.2.230:5188/files/xy_playback?target_path="

    var nasMid: Int = 0

    private const val SOURCE_PREFIX = "http://resources-cloud.singworld.cn/"
    private const val SOURCE_PREFIX1 = "https://resources-cloud.singworld.cn/"
    private const val SOURCE_PREFIX2 = "http://aam-net-front-public.singworld.cn"
    private const val SOURCE_PREFIX3 = "https://aam-net-front-public.singworld.cn"
    private val scope = CoroutineScope(Dispatchers.Default)


    /**
     * 获取Nas url地址
     * persist.aam.proxy.type
     * 0 --> 返回原url
     * 1 --> 使用主机本地代理服务
     * 2 --> 使用nas缓存
     * 默认使用机器本地代理
     *
     */
    suspend fun getNasUrl(url: String): String {
        val type = PropertiesUtil.getSystemProperty(PROP_AAM_PROXY_TYPE)
        //LogUtils.d(TAG, "getNasUrl type = $type")
        when (type) {
            "0" -> {
                return url
            }

            "1" -> {
                return getProxyServerUrl(url)
            }

            "2" -> {
//                if (hasNasIp.not()) {
//                    return getProxyServerUrl(url)
//                }
                val path = URL(url).path
                val encodedString = withContext(Dispatchers.IO) {
                    URLEncoder.encode("/XiangYun$path", "UTF-8")
                }
                val temp = NAS_BASE_URL + encodedString
//                LogUtils.d(TAG, "nas url: $temp")
                val async = scope.async { isHttpEndpointReachable(temp) }
                val result = async.await()
                LogUtils.d(TAG, "nas head result = $result ,url = $temp")
                if (result.not()) {
                    // head 失败
                    scope.launch(Dispatchers.IO) {
                        try {
                            val param = mapOf(
                                "mid" to GlobalVariable.mid,
                                "nas_mid" to nasMid,
                                "reason" to "HEAD failed",
                                "timestamp" to System.currentTimeMillis() / 1000
                            )
                            WSManager.sendMessage(ApiConstant.REQ_REPORT_NAS_ERROR_MSG,
                                param, String::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            LogUtils.e(TAG, "report nas error failed: ${e.message}")
                        }
                    }
                }
                return if (result) {
                    temp
                } else {
                    getProxyServerUrl(url)
                }
            }

            else -> {
                return getProxyServerUrl(url)
            }
        }
    }


    fun getProxyServerUrl(url: String): String {
        if (systemVersion.isEmpty()) {
            systemVersion =
                (SystemPropertyConstant.getSystemVersion() ?: "")
                    .split("-")[0].replace(
                    "v",
                    ""
                )
        }
        Log.i(TAG, systemVersion)
        if (compareVersion(systemVersion, "2.4") < 0) {
            Log.i(TAG, "小于2.4版本不启动资源代理")
            return url
        }
        var ss: ServerSocket? = null
        try {
            ss = ServerSocket(PORT)
            isProxyValid = false
            Log.i(TAG, "代理服务器未运行")
        } catch (e: Exception) {
            Log.i(TAG, "端口已绑定，代理服务器已运行")
            isProxyValid = true
        } finally {
            try {
                ss?.close()
            } catch (e: Exception) {
            }
        }

        if (!isProxyValid) {
            return url
        }

        //http url 且 不是m3u8后缀
        if (url.startsWith("http", ignoreCase = true) && Uri.parse(url).path?.endsWith(
                "m3u8",
                ignoreCase = true
            ) != true
        ) {

            return "http://127.0.0.1:10231/v1/video/proxy?videoUrl=" +
                    URLEncoder.encode(url, "utf-8")

        }
        return url
    }

    private fun compareVersion(version1: String, version2: String): Int {
        if (version1 == version2) {
            return 0
        }
        val version1Array = version1.trim().split(".")
        val version2Array = version2.trim().split(".")
        var index = 0
        var minLen = Math.min(version1Array.size, version2Array.size)
        var diff = 0
        while (index < minLen) {
            try {
                diff = version1Array[index].toInt() - version2Array[index].toInt()
            } catch (e: NumberFormatException) {
                diff = version1Array[index].compareTo(version2Array[index])
            }
            if (diff != 0) {
                break
            }
            index++
        }

        if (diff == 0) {
            if (version1Array.size > minLen) {
                for (i in index until version1Array.size) {
                    try {
                        if (version1Array[i].isNotBlank() && version1Array[i].toInt() > 0) {
                            return 1
                        }
                    } catch (e: NumberFormatException) {
                        return 1
                    }
                }
                for (i in index until version2Array.size) {
                    try {
                        if (version2Array[i].isNotBlank() && version2Array[i].toInt() > 0) {
                            return -1
                        }
                    } catch (e: NumberFormatException) {
                        return -1
                    }

                }
            } else {

            }
        } else {
            return if (diff > 0) 1 else -1
        }
        return 0
    }


    private fun isHttpEndpointReachable(urlString: String): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.setConnectTimeout(500) // 设置连接超时时间
            connection.setReadTimeout(500) // 设置读取超时时间
            connection.setRequestMethod("HEAD") // 使用HEAD方法进行请求，以减少数据传输
            val responseCode = connection.getResponseCode()
            LogUtils.d(TAG, "HEAD responseCode : $responseCode")
            responseCode in 200..299 // 判断响应码是否表示成功
        } catch (e: Exception) {
            LogUtils.e(TAG, "HEAD err " + e.message)
            false
        } finally {
            connection?.disconnect()
        }
    }
}