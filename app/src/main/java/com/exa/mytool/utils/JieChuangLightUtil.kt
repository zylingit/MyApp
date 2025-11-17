package com.exa.mytool.utils

import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.mida.base.ext.currentActivity
import com.aam.mida.mida_yk.activity.KTVPlayActivity
import com.aam.soundsetting.ProductType
import com.am.websocket.ApiConstant
import com.am.websocket.WSManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 上报灯光状态，用在GS68上(杰创)
 */
fun reportJieChuangLightState(customValue: Boolean? = null) {
    if (GlobalVariable.produceType != ProductType.F3P_GS68_2371101) {
        return
    }
    GlobalScope.launch(Dispatchers.IO) {
        // GlobalVariable.KTVLightState
        val isOpen = when {
            customValue != null -> {
                customValue
            }
            currentActivity is KTVPlayActivity -> {
                GlobalVariable.KTVLightState?: false
            }
            else -> {
                false
            }
        }

        val value = if (isOpen) 1 else 0
        val param = mutableMapOf("mid" to GlobalVariable.mid, "uid" to -1,
            "type" to "light", "value" to value)
        try {
            WSManager.sendMessage(ApiConstant.REQ_MACHINE_OPERATE, param, String::class.java)
        } catch (e: Exception) {
            LogUtils.w("JieChuangLightUtil", "上报灯光状态失败: ${e.message}")
        }
    }
}