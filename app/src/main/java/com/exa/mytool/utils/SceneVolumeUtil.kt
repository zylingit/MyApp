package com.exa.mytool.utils

import com.aam.loglibs.LogUtils
import com.aam.mida.base.base.BaseActivity
import com.aam.mida.base.ext.activityList
import com.aam.mida.base.ext.currentActivity
import com.aam.mida.mida_yk.YKApplication
import com.aam.mida.mida_yk.activity.KTVPlayActivityVolumeDelegate
import com.aam.mida.mida_yk.activity.MainActivity
import com.aam.mida.mida_yk.constant.AAMServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var lastScene = ""
private set

/**
 * @param scene 场景 取值范围：[VolumeManager.SCENE_STANDBY]、[VolumeManager.SCENE_APP]
 * @param isForce 是否强制更新
 */
fun resetToSceneVolume(scene: String, isForce: Boolean = false): Boolean {
//    val scene = getCurrentScene()
    val sceneName = if (scene == VolumeManager.SCENE_STANDBY) {
        VolumeManager.SCENE_STANDBY
    } else {
        VolumeManager.SCENE_APP
    }

    if (lastScene == sceneName && isForce.not()) {
        LogUtils.d("SceneVolumeUtil", "the same scene $sceneName, ignore.")
        return false
    }

    lastScene = sceneName
//    val viewModel = PreferenceSettingsViewModel()
    val defaultVolume = VolumeManager.getDefaultMusicVolume(sceneName)
//    val currentVolume = VolumeManager.getMusicVolume(sceneName)
    val newVolume = calculateNewSceneVolume(sceneName)
//    if (currentVolume > defaultVolume) {
//        VolumeManager.setMusicVolume(scene, defaultVolume)
//        viewModel.setSceneVolume("music", defaultVolume)
//        LogUtils.d("SceneVolumeUtil", "reset scene volume: " +
//            "$scene(${activity.javaClass.simpleName}) default: $defaultVolume, current: $defaultVolume")
//    } else {
//        viewModel.setSceneVolume("music", currentVolume)
//        LogUtils.d("SceneVolumeUtil", "reset scene volume: " +
//            "$scene(${activity.javaClass.simpleName}) default: $defaultVolume, current: $currentVolume")
//    }

    VolumeManager.setMusicVolume(sceneName, newVolume)
//    viewModel.setSceneVolume("music", newVolume)
    AAMServiceManager.setDSPVolume("music", newVolume, showDialog = false, isSceneVolume = true)
    LogUtils.d("SceneVolumeUtil", "reset scene volume: $sceneName default: $defaultVolume, current: $newVolume")

//    viewModel.setSceneVolume("mic", VolumeManager.getVolume(VolumeManager.VOLUME_TYPE_MIC))
    AAMServiceManager.setDSPVolume("mic", VolumeManager.getVolume(VolumeManager.VOLUME_TYPE_MIC),
        showDialog = false, isSceneVolume = true)
//    viewModel.setSceneVolume("effector", VolumeManager.getVolume(VolumeManager.VOLUME_TYPE_EFFECTOR))
    AAMServiceManager.setDSPVolume("effector", VolumeManager.getVolume(VolumeManager.VOLUME_TYPE_EFFECTOR),
        showDialog = false, isSceneVolume = true)

    return true
}

fun calculateNewSceneVolume(scene: String): Int {
    val defaultVolume = VolumeManager.getDefaultMusicVolume(scene)
    val currentVolume = VolumeManager.getMusicVolume(scene)
    return if (currentVolume > defaultVolume) {
        defaultVolume
    } else {
        currentVolume
    }
}

fun currentSceneFromActivity(activity: BaseActivity): String {
    return activity.currentScene()
}

/**
 * 从当前有效的Activity中获取场景
 */
fun currentSceneFromActiveActivity(): String {
    return if (currentActivity?.isFinishing == true) {
        if (activityList.size <= 1 &&
            (activityList.firstOrNull() !is MainActivity && activityList.firstOrNull() !is com.aam.mida.mida_yk.activity.touch.MainActivity)
            ) {
            "scene_standby"
        } else {
            activityList.getOrNull(activityList.size - 2)?.currentScene()?: "scene_standby"
        }
    } else {
        currentActivity?.currentScene()?: "scene_standby"
    }
}

/**
 * 上报音量到小程序
 */
fun reportVolume() {
    GlobalScope.launch(Dispatchers.IO) {
        // 上报小程序
        val delegate = KTVPlayActivityVolumeDelegate(YKApplication.app)
//        val dspVolumeList = AAMServiceManager.getDSPVolumeList()
        val currentScene = withContext(Dispatchers.Main) {
            lastScene
        }
        if (currentScene.isEmpty()) {
            delegate.onDestroy()
            LogUtils.d("SceneVolumeUtil", "currentScene is empty, ignore")
            return@launch
        }
        val dspVolumeList = listOf(
            VolumeManager.getMusicVolume(currentScene),
            VolumeManager.getVolume(VolumeManager.VOLUME_TYPE_MIC),
            VolumeManager.getVolume(VolumeManager.VOLUME_TYPE_EFFECTOR))
        LogUtils.d("SceneVolumeUtil", "reportVolume: ${dspVolumeList[0]}, ${dspVolumeList[1]}, ${dspVolumeList[2]}")
        listOf(async { delegate.pushVolumeToServer1(dspVolumeList[0], 1) },
            async { delegate.pushVolumeToServer1(dspVolumeList[1], 2) },
        async { delegate.pushVolumeToServer1(dspVolumeList[2], 3) }).awaitAll()

        delegate.onDestroy()
    }
}

/**
 * 上报音量到小程序
 */
fun reportVolume(music: Int, mic: Int, effect: Int) {
    GlobalScope.launch(Dispatchers.IO) {
        // 上报小程序
        val delegate = KTVPlayActivityVolumeDelegate(YKApplication.app)
//        val dspVolumeList = AAMServiceManager.getDSPVolumeList()
        val dspVolumeList = listOf(music, mic, effect)
        LogUtils.d("SceneVolumeUtil", "reportVolume: ${dspVolumeList[0]}, ${dspVolumeList[1]}, ${dspVolumeList[2]}")
        listOf(async { delegate.pushVolumeToServer1(dspVolumeList[0], 1) },
            async { delegate.pushVolumeToServer1(dspVolumeList[1], 2) },
            async { delegate.pushVolumeToServer1(dspVolumeList[2], 3) }).awaitAll()

        delegate.onDestroy()
    }
}

fun updateScene(newScene: String) {
    lastScene = when(newScene) {
        VolumeManager.SCENE_STANDBY -> VolumeManager.SCENE_STANDBY
        else -> VolumeManager.SCENE_APP
    }
}