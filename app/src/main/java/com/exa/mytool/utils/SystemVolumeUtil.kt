package com.exa.mytool.utils

import android.app.Service
import android.content.Context
import android.media.AudioManager

/**
 * 系统音量相关
 */
class SystemVolumeUtil(context: Context) {

    private val mAudioManager: AudioManager =
        context.getSystemService(Service.AUDIO_SERVICE) as AudioManager

    private val type = AudioManager.STREAM_MUSIC

    fun getMaxSystemVolume() = mAudioManager.getStreamMaxVolume(type)

    fun getMinSystemVolume() = 0

    fun getCurrentSystemVolume() = mAudioManager.getStreamVolume(type)

    fun setSystemVolume(
        value: Int, flags: Int = (AudioManager.FLAG_PLAY_SOUND
                or AudioManager.FLAG_SHOW_UI)
    ) = mAudioManager.setStreamVolume(
        type,
        value,
        flags
    )
}