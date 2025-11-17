package com.exa.mytool.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.SoundPool
import com.aam.loglibs.LogUtils
import com.aam.mida.mida_yk.R
import com.aam.mida.mida_yk.YKApplication

class SoundPlayerUtils {
    enum class RingerTypeEnum {
        SOUND_ONE,
        SOUND_TWO,
        SOUND_THREE,
        SOUND_FOUR
    }

    private val context: Context = YKApplication.app //全局上下文对象，getApplicationContext();


    private var soundPool: SoundPool? = null
    private var audioManager: AudioManager? = null
    private var streamId = 0
    private var soundId = 0
    private val loop = false
    private var ringerTypeEnum: RingerTypeEnum = RingerTypeEnum.SOUND_ONE
    private var isRingModeRegister = false
    private var ringMode = -1


    private var ringModeChangeReceiver: RingModeChangeReceiver? = null


    @Synchronized
    fun play(type: RingerTypeEnum = RingerTypeEnum.SOUND_TWO) {
        LogUtils.e(TAG, "play type->" + type!!.name)
        this.ringerTypeEnum = type
        play(R.raw.ml)
    }


    fun stop() {
        LogUtils.e(TAG, "stop")
        if (soundPool != null) {
            if (streamId != 0) {
                soundPool!!.stop(streamId)
                streamId = 0
            }
            if (soundId != 0) {
                soundPool!!.unload(soundId)
                soundId = 0
            }
        }
        if (isRingModeRegister) {
            registerVolumeReceiver(false)
        }
    }


    private fun play(ringId: Int) {
        initSoundPool()
        if (audioManager?.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            soundId = soundPool?.load(context, ringId, 1)?:0
        }
    }


    private fun initSoundPool() {
        stop()
        if (soundPool == null) {
            soundPool = SoundPool(1, AudioManager.STREAM_RING, 0)
            soundPool?.setOnLoadCompleteListener(onLoadCompleteListener)
            soundPool?.setVolume(0, 1f, 1f)

            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            ringMode = audioManager!!.ringerMode
        }
        registerVolumeReceiver(true)
    }


    var onLoadCompleteListener: SoundPool.OnLoadCompleteListener =
        SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
            if (soundId != 0 && status == 0) {
                if (audioManager?.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                    val curVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_RING)
                    streamId = soundPool.play(
                        soundId,
                        curVolume.toFloat(),
                        curVolume.toFloat(),
                        1,
                        if (loop) -1 else 0,
                        1f
                    )
                }
            }
        }


    private fun registerVolumeReceiver(register: Boolean) {
        if (ringModeChangeReceiver == null) {
            ringModeChangeReceiver = RingModeChangeReceiver()
        }


        if (register) {
            isRingModeRegister = true
            val filter = IntentFilter()
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            context.registerReceiver(ringModeChangeReceiver, filter)
        } else {
            context.unregisterReceiver(ringModeChangeReceiver)
            isRingModeRegister = false
        }
    }


    private inner class RingModeChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ringMode != -1 && ringMode != audioManager?.ringerMode && intent.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                ringMode = audioManager?.ringerMode?:-1
                play(ringerTypeEnum)
            }
        }
    }

    companion object {
        private const val TAG = "SoundPlayerUtils"


        @SuppressLint("StaticFieldLeak")
        var instance: SoundPlayerUtils? = null
            get() {
                if (field == null) {
                    synchronized(SoundPlayerUtils::class.java) {
                        if (field == null) {
                            field = SoundPlayerUtils()
                        }
                    }
                }
                return field
            }
            private set
    }
}
