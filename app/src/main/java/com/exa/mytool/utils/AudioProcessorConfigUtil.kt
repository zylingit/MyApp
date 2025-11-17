package com.aam.mida.mida_yk.utils

import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.soundsetting.ProductType
import com.aam.soundsetting.VDSocket2
import com.aam.soundsetting.VirtualDeviceSDK
import com.jan.aammedia.AudioProcessorConfig

private const val TAG = "AudioProc"

fun getAudioProcessorConfig() = AudioProcessorConfig(true,
    GlobalVariable.produceType != ProductType.F0P_HS28_2214).apply {
//    isEnableAudioMixer = GlobalVariable.produceType != ProductType.F0P_HS28_2214
}

fun processAudioInputChannel(channel:Int) {
    if (GlobalVariable.produceType == ProductType.F1V_22117) {
        val channelInfo = when (channel) {
            8 -> {
                "7.1"
            }

            6 -> {
                "5.1"
            }

            else -> {
                "2.0"
            }
        }
        VirtualDeviceSDK.getInstance().reqSetSoundChannelInfo(channelInfo, object :
            VDSocket2.MessageListener() {
            override fun onSuccess(data: String?) {
                LogUtils.i(TAG, "$data")
            }

            override fun onError(e: java.lang.Exception) {
                LogUtils.e(TAG, e)
            }
        })
    }

}

