package com.aam.mida.mida_yk.utils

import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.mida.base.utils.SPUtils
import com.aam.mida.mida_yk.YKApplication
import com.aam.mida.mida_yk.entity.VolumeGroup
import com.aam.soundsetting.ProductType

object VolumeManager {

    private val TAG = "VolumeManager"

    /**
     * 配置文件的名字
     */
    const val CONFIG_FILE_NAME = "volume_group"

    /**
     * 场景：待机
     */
    const val SCENE_STANDBY = "scene_standby"

    /**
     * 场景：KTV
     */
    @Deprecated("该属性已过时，请使用[SCENE_APP]", replaceWith = ReplaceWith(expression = "VolumeManager.SCENE_APP"))
    val SCENE_KTV = "scene_ktv"

    /**
     * 场景：movie
     */
    @Deprecated("该属性已过时，请使用[SCENE_APP]", replaceWith = ReplaceWith(expression = "VolumeManager.SCENE_APP"))
    val SCENE_MOVIE = "scene_movie"

    /**
     * 场景：APP
     */
    val SCENE_APP = "scene_app"

    /**
     * 音乐音量
     */
    const val VOLUME_TYPE_MUSIC = "music"

    /**
     * 麦克风音量
     */
    const val VOLUME_TYPE_MIC = "mic"

    /**
     * 效果音
     */
    const val VOLUME_TYPE_EFFECTOR = "effector"

    /**
     * 默认音量
     */
    private val defaultVolume = VolumeGroup(50, 50, 50)

    /**
     * 待机音量组
     */
    private lateinit var standbyVolumeGroup: VolumeGroup

    /**
     * 多媒体音量组
     */
    private lateinit var mediaVolumeGroup: VolumeGroup

    /**
     * 存音量的键值对
     */
    private val volumeValueMap = HashMap<String, Int>()

    fun init() {
        if(ProductType.isSupportSceneVolume(GlobalVariable.produceType)){

            // 重置音量为默认音量
            // 待机音量
            setMusicVolume(SCENE_STANDBY, getDefaultMusicVolume(SCENE_STANDBY))

            // APP音量
            setMusicVolume(SCENE_APP, getDefaultMusicVolume(SCENE_APP))

            setVolume(VOLUME_TYPE_MIC, getDefaultVolume(VOLUME_TYPE_MIC))
            setVolume(VOLUME_TYPE_EFFECTOR, getDefaultVolume(VOLUME_TYPE_EFFECTOR))
        }
    }

    /**
     * 快速获取麦克风音量、效果音量值
     * @param volumeType 音量类型，取值范围为：[VOLUME_TYPE_MIC], [VOLUME_TYPE_EFFECTOR]
     */
    fun getVolume(volumeType: String): Int {
        if (volumeType != VOLUME_TYPE_MUSIC && volumeType != VOLUME_TYPE_MIC
            && volumeType != VOLUME_TYPE_EFFECTOR) {
            throw java.lang.IllegalArgumentException("不合法的参数volumeType: $volumeType")
        }
        var result = volumeValueMap["$volumeType"] ?: -1
        if (result == -1) {
            result = when (volumeType) {
                VOLUME_TYPE_MUSIC -> {
                    defaultVolume.music
                }
                VOLUME_TYPE_MIC -> {
                    defaultVolume.mic
                }
                else -> {
                    defaultVolume.effector
                }
            }
        }
        LogUtils.d(TAG, "get $volumeType volume: $result")
        return result
    }

    /**
     * 获取音量
     * @param scene 场景，取值范围为: [SCENE_STANDBY], [SCENE_APP]
     */
    fun getMusicVolume(scene: String): Int {
        if (scene != SCENE_STANDBY && scene != SCENE_APP && scene != SCENE_KTV && scene != SCENE_MOVIE) {
            throw java.lang.IllegalArgumentException("不合法的参数scene: $scene")
        }

        val sceneName = when(scene) {
            SCENE_STANDBY -> {
                SCENE_STANDBY
            }
            else -> SCENE_APP
        }

        var result = volumeValueMap["${sceneName}_music"] ?: -1
        if (result == -1) {
            result = defaultVolume.music
        }
        LogUtils.d(TAG, "get $sceneName music volume: $result")
        return result
    }

    /**
     * 设置麦克风音量或者效果音量，该方法仅用作保存当前的音量值，便于快速查询
     * @param volumeType 音量类型，取值范围为：[VOLUME_TYPE_MIC], [VOLUME_TYPE_EFFECTOR]
     * @param volume 音量值，取值范围: 0 <= volume <= 100
     */
    fun setVolume(volumeType: String, volume: Int) {
        if (volumeType != VOLUME_TYPE_MIC
            && volumeType != VOLUME_TYPE_EFFECTOR) {
            throw java.lang.IllegalArgumentException("不合法的参数volumeType: $volumeType")
        }

        val realVolume = if (volume < 0) {
            0
        } else if (volume > 100) {
            100
        } else {
            volume
        }

        LogUtils.d(TAG, "set $volumeType volume: $realVolume")
        volumeValueMap.put("$volumeType", realVolume)
    }

    /**
     * 设置用户音量
     * @param scene 场景，取值范围为: [SCENE_STANDBY]，[SCENE_APP]
     * @param volume 音量值，取值范围: 0 <= volume <= 100
     */
    fun setMusicVolume(scene: String, volume: Int) {
        if (scene != SCENE_STANDBY && scene != SCENE_APP && scene != SCENE_KTV && scene != SCENE_MOVIE) {
            throw java.lang.IllegalArgumentException("不合法的参数scene: $scene")
        }

        val realVolume = if (volume < 0) {
            0
        } else if (volume > 100) {
            100
        } else {
            volume
        }

        val sceneName = when(scene) {
            SCENE_STANDBY -> {
                SCENE_STANDBY
            }
            else -> SCENE_APP
        }

        LogUtils.d(TAG, "set $sceneName music volume: $realVolume")
        volumeValueMap["${sceneName}_music"] = realVolume
    }

    /**
     * 获取默认音量
     * @param scene 场景，取值范围为: [SCENE_STANDBY], [SCENE_APP]
     */
    fun getDefaultMusicVolume(scene: String): Int {
        if (scene != SCENE_STANDBY && scene != SCENE_APP && scene != SCENE_KTV && scene != SCENE_MOVIE) {
            throw java.lang.IllegalArgumentException("不合法的参数scene: $scene")
        }

        val defaultValue = if (scene == SCENE_STANDBY) {
            // 待机音乐音量，默认30
            30
        } else {
            defaultVolume.music
        }

        val sceneName = when(scene) {
            SCENE_STANDBY -> {
                SCENE_STANDBY
            }
            else -> SCENE_APP
        }

        val result = SPUtils[YKApplication.app, CONFIG_FILE_NAME, "default_${sceneName}_music", defaultValue] as Int
        LogUtils.d(TAG, "get $sceneName music default volume: $result")
        return result
    }

    /**
     * 设置默认音量
     * @param scene 场景，取值范围为: [SCENE_STANDBY], [SCENE_APP]
     * @param volume 音量值，取值范围: 0 <= volume <= 100
     */
    fun setDefaultMusicVolume(scene: String, volume: Int) {
        if (scene != SCENE_STANDBY && scene != SCENE_APP && scene != SCENE_KTV && scene != SCENE_MOVIE) {
            throw java.lang.IllegalArgumentException("不合法的参数scene: $scene")
        }

        val realVolume = if (volume < 0) {
            0
        } else if (volume > 100) {
            100
        } else {
            volume
        }

        val sceneName = when(scene) {
            SCENE_STANDBY -> {
                SCENE_STANDBY
            }
            else -> SCENE_APP
        }

        LogUtils.d(TAG, "set $sceneName music default volume: $realVolume")
        SPUtils.put(YKApplication.app, CONFIG_FILE_NAME, "default_${sceneName}_music", realVolume)

        // 待机音量
        if (sceneName == SCENE_STANDBY) {
            setMusicVolume(SCENE_STANDBY, volume)
        } else if (sceneName == SCENE_APP) {
            // APP音量
            setMusicVolume(SCENE_APP, volume)
        }
    }

    /**
     * 获取默认音量
     * @param volumeType 音量类型，取值范围为: [VOLUME_TYPE_MIC], [VOLUME_TYPE_EFFECTOR]
     */
    fun getDefaultVolume(volumeType: String): Int {
        val defaultValue = if (volumeType == VOLUME_TYPE_MIC) {
            defaultVolume.mic
        } else {
            defaultVolume.effector
        }


        val result = SPUtils[YKApplication.app, CONFIG_FILE_NAME, "default_$volumeType", defaultValue] as Int
        LogUtils.d(TAG, "get $volumeType default volume: $result")
        return result
    }

    /**
     * 设置默认音量
     * @param volumeType 场景，取值范围为: [VOLUME_TYPE_MIC], [VOLUME_TYPE_EFFECTOR]
     * @param volume 音量值，取值范围: 0 <= volume <= 100
     */
    fun setDefaultVolume(volumeType: String, volume: Int) {
        val realVolume = if (volume < 0) {
            0
        } else if (volume > 100) {
            100
        } else {
            volume
        }

        LogUtils.d(TAG, "set $volumeType default volume: $realVolume")
        SPUtils.put(YKApplication.app, CONFIG_FILE_NAME, "default_${volumeType}", realVolume)
    }
}