package utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.lang.reflect.Method


object WifiApUtil {
    val TAG = "WIFIAP"

    interface WifiApCallback {
        fun onSuccess(ssid: String, password: String)
        fun onFail(error: String)
    }

    private fun getMethodByName(clazz: Class<*>, methodName: String): Method? {
        return clazz.methods.find {
            it.name == methodName
        }
    }

    @SuppressLint("MissingPermission")
    fun setWifiAp(c: Context, ssidStr: String, passStr: String, callback: WifiApCallback?) {
        try {
            val wifiManager = c.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val stopApMethod = getMethodByName(WifiManager::class.java, "stopSoftAp")
                ?: throw Exception("stopSoftAp not support")
            try {
                stopApMethod.invoke(wifiManager)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(TAG, "stopApFail")
            }

            val startApMethod = getMethodByName(WifiManager::class.java, "startTetheredHotspot")
                ?: throw Exception("startTetheredHotspot not support")
            val configMethod =
                getMethodByName(WifiManager::class.java, "getSoftApConfiguration")
                    ?: throw Exception("getSoftApConfiguration not support")
            val config = configMethod.invoke(wifiManager)
            val clazz = Class.forName("android.net.wifi.SoftApConfiguration\$Builder")
            val buildConstructorMethod = clazz.getConstructor(config.javaClass)
            val builderInstance = buildConstructorMethod.newInstance(config)
            val passMethod = getMethodByName(clazz, "setPassphrase")
                ?: throw Exception("setPassphrase not support")
            val ssidMethod = getMethodByName(clazz, "setSsid")
                ?: throw Exception("setSsid not support")
            val autoShutdownEnable = getMethodByName(clazz, "setAutoShutdownEnabled")
                ?: throw Exception("setAutoShutdownEnabled not support")
            val buildMethod =
                getMethodByName(clazz, "build") ?: throw Exception("build not support")
            //SECURITY_TYPE_WPA2_PSK = 1
            passMethod.invoke(builderInstance, passStr, 1)
            ssidMethod.invoke(builderInstance, ssidStr)
            autoShutdownEnable.invoke(builderInstance, false)
            val newConfig = buildMethod.invoke(builderInstance);

            val result = startApMethod.invoke(wifiManager, newConfig)
            if (result is Boolean && result == true) {
                callback?.onSuccess(ssidStr, passStr)
                return

            }
            throw Exception("hotspot start result err:${result}")

        } catch (e: Exception) {
            e.printStackTrace()
            callback?.onFail("发生异常：${e.message}")
        }

    }
}