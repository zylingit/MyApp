package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.DiffCallback
import androidx.leanback.widget.ItemBridgeAdapter
import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.mida.base.anim.BaseOnKeyListener
import com.aam.mida.base.base.appContext
import com.aam.mida.base.utils.ToastUtil
import com.aam.mida.base.widgets.AAMFloatingDialog
import com.aam.mida.mida_yk.R
import com.aam.mida.mida_yk.YKApplication
import com.aam.mida.mida_yk.databinding.WindowSettingBinding
import com.aam.mida.mida_yk.devicectl.DeviceCtlCmd
import com.aam.mida.mida_yk.devicectl.DeviceCtlManager
import com.aam.mida.mida_yk.entity.ManualModeEntity
import com.aam.mida.mida_yk.entity.SmartScenesEntity
import com.aam.mida.mida_yk.leanback.presenter.LightingScenelPresenter
import com.aam.mida.mida_yk.leanback.presenter.ManualModePresenter
import com.aam.mida.mida_yk.observer.SettingWindowCallback
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_DONG_GAN
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_JI_QING
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_LANG_MAN
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_MENG_LONG
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_MING_LIANG
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_MING_MEI
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_MODE_1
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_MODE_1_1
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_OFF
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_ROU_HE
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_SHU_QING
import com.aam.mida.mida_yk.receiver.MachineLightModeChangeReceiver.Companion.BLE_LIGHT_XUAN_XIU
import com.aam.mida.mida_yk.service.SettingWindow
import com.aam.mida.mida_yk.utils.GsonUtils
import com.aam.mida.mida_yk.utils.PreInflateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * @author          lzb
 * @time            2024年9月26日
 * @des             灯光场景
 *
 * @version         1.0v$
 * @updateAuthor    $
 * @updateDate      $
 * @updateDes       灯光场景控制
 */
class LightingSceneView{

    private val TAG =  this.javaClass.simpleName

    val scope = MainScope()

    private val mSmartScenesEntityList = mutableListOf<SmartScenesEntity>()
    private var mLightingScenelAdapter: ArrayObjectAdapter? = null
    private var mLightingScenelPresenter: LightingScenelPresenter? = null
    var lightingScenelHelper: PreInflateHelper = PreInflateHelper(8)

    private var settingBinding: WindowSettingBinding? = null
    private var mContext: Context? = null

    private val lightModeRefreshChannel = Channel<String>(capacity = Channel.CONFLATED)

//    private var mManualModeAdapter: ManualModeAdapter? = null
    /**
     * Adapter
     */
    private var mManualModeAdapter: ArrayObjectAdapter? = null
    private var mManualModePresenter: ManualModePresenter? = null
    var manualModePreInflateHelper: PreInflateHelper = PreInflateHelper(5)
    private val mManualModeEntityList = mutableListOf<ManualModeEntity>()

    private val lightSceneChangeListener: (String) -> Unit = { newScene ->
        LogUtils.d(TAG, "new light scene: $newScene")


        mSmartScenesEntityList.find {
            it.select
        }?.let {
            it.select = false
        }

        mSmartScenesEntityList.find {
            it.id == newScene
        }?.let {
            it.select = true
        }

        mLightingScenelAdapter?.notifyItemRangeChanged(0, mSmartScenesEntityList.size)

        val lightingSceneHasFocus = settingBinding?.includeLighting?.rvLightingScene?.hasFocus()?: false
        // 优化智能场景模式体验，滑动到选中的Item
        scrollToSelectedItemInSmartMode()
        if (lightingSceneHasFocus) {
            settingBinding?.includeLighting?.rvLightingScene?.requestFocus()
        }

        setManualModeEnable(newScene)
    }

    var settingWindowCallback: SettingWindowCallback? = null

    var degree:Int = 28
    var progress:Float = 0f

    /**
     * 是否绑定蓝牙灯光控制器
     */
    private var isBleLightControllerBind = false

    /**
     * 左侧优先显示上一个activity的title
     */
    constructor(context: Context, windowSettingBinding: WindowSettingBinding) {
        settingBinding = windowSettingBinding
        mContext = context
        init()
    }

    fun init(){
        initLightingScenel()
        initManualMode()
        settingBinding?.includeLighting?.tvSmartLightingScenes?.isSelected = true
        // 优化智能场景模式体验，滑动到选中的Item
        scrollToSelectedItemInSmartMode()

        YKApplication.app.machineLightModeChangeReceiver.registerLightSceneChangeListener(lightSceneChangeListener)
    }

    private fun setManualModeEnable(scene: String) {
        when (scene) {
            MachineLightModeChangeReceiver.MODE_1,
            MachineLightModeChangeReceiver.MODE_1_1,
            MachineLightModeChangeReceiver.MODE_OFF,
            BLE_LIGHT_OFF,
            BLE_LIGHT_MODE_1,
            BLE_LIGHT_MODE_1_1,
            BLE_LIGHT_MENG_LONG,
            BLE_LIGHT_DONG_GAN,
            BLE_LIGHT_MING_LIANG,
            BLE_LIGHT_SHU_QING,
            BLE_LIGHT_ROU_HE,
            BLE_LIGHT_LANG_MAN,
            BLE_LIGHT_XUAN_XIU,
            BLE_LIGHT_JI_QING,
            BLE_LIGHT_MING_MEI,
                -> {
                settingBinding?.includeLighting?.tvSmartLightingScenes?.let {
                    it.nextFocusRightId = it.id
                }
            }
            else -> {
                settingBinding?.includeLighting?.tvSmartLightingScenes?.let {
                    it.nextFocusRightId = settingBinding?.includeLighting?.tvManualModeLight?.id?: View.NO_ID
                }
            }
        }
    }

    fun onViewVisible() {
        settingBinding?.includeLighting?.lightingSceneLayout?.post {
            if (mSmartScenesEntityList.first().id != YKApplication.app.machineLightModeChangeReceiver.getLightSceneModeList().first().id) {
                // 模式列表发生变化
                val currentMode = YKApplication.app.machineLightModeChangeReceiver.currentMode
                mSmartScenesEntityList.clear()
                mSmartScenesEntityList.addAll(YKApplication.app.machineLightModeChangeReceiver.getLightSceneModeList())
                mSmartScenesEntityList.forEach {
                    it.select = it.id == currentMode
                }
                mLightingScenelAdapter?.clear()
                mLightingScenelAdapter?.addAll(0, mSmartScenesEntityList)
            } else {
                val newScene = YKApplication.app.machineLightModeChangeReceiver.currentMode

                mSmartScenesEntityList.find {
                    it.select
                }?.let {
                    it.select = false
                }

                mSmartScenesEntityList.find {
                    it.id == newScene
                }?.let {
                    it.select = true
                }

                mLightingScenelAdapter?.notifyItemRangeChanged(0, mSmartScenesEntityList.size)

            }

            // 优化智能场景模式体验，滑动到选中的Item
            scrollToSelectedItemInSmartMode()

            if (settingBinding?.includeLighting?.tvSmartLightingScenes?.isSelected != true) {
                showSongCountTab(true, false)
//                    switchView(settingBinding?.includeLighting?.clLightingSceneLayout!!,settingBinding?.includeLighting?.manualModeLightLayout!!,settingBinding?.includeLighting?.arcControlModeLayout!!)
                settingBinding?.includeLighting?.tvManualModeLight!!.isSelected = false
                slideToRight(settingBinding?.includeLighting?.manualModeLightLayout!!, settingBinding?.includeLighting?.clLightingSceneLayout!!)
            }
        }

        setManualModeEnable(YKApplication.app.machineLightModeChangeReceiver.currentMode)
    }

    private fun initManualModeListData() {
        mManualModeEntityList.clear()
        val receiver = YKApplication.app.machineLightModeChangeReceiver
        val currentModeId = receiver.currentMode
        val config = receiver.modeColorMap[currentModeId]
        if (config == null) {
            if (receiver.hasScreenLight()) {
                mManualModeEntityList.add(ManualModeEntity("1",
                    YKApplication.app.getString(R.string.movie_screen_light),"1",false,30,
                    Color.GREEN, 300f))
            }

            if (receiver.hasCeilingLight()) {
                mManualModeEntityList.add(ManualModeEntity("2",
                    YKApplication.app.getString(R.string.machine_light_ceiling_lights),"2",false,30,
                    Color.GREEN, 300f))
            }

            if (receiver.hasDMXLight()) {
                mManualModeEntityList.add(ManualModeEntity("3",
                    YKApplication.app.getString(R.string.movie_mood_lighting),"3",false,30,
                    Color.GREEN,300f))
            }
        } else {
            if (receiver.hasScreenLight()) {
                mManualModeEntityList.add(ManualModeEntity("1",
                    YKApplication.app.getString(R.string.movie_screen_light),
                    "1",
                    config.screenLightConfig.isOn,
                    config.screenLightConfig.brightness,
                    config.screenLightConfig.color,
                    300f))
            }

            if (receiver.hasCeilingLight()) {
                mManualModeEntityList.add(ManualModeEntity("2",
                    YKApplication.app.getString(R.string.machine_light_ceiling_lights),
                    "2",
                    config.ceilingLightConfig.isOn,
                    config.ceilingLightConfig.brightness,
                    config.ceilingLightConfig.color,
                    300f))
            }

            if (receiver.hasDMXLight()) {
                mManualModeEntityList.add(ManualModeEntity("3",
                    YKApplication.app.getString(R.string.movie_mood_lighting),
                    "3",
                    config.ambientLightConfig.isOn,
                    config.ambientLightConfig.brightness,
                    config.ambientLightConfig.color,
                    300f))
            }
        }

//        mManualModeAdapter?.clear()
//        mManualModeAdapter?.addAll(0, mManualModeEntityList)
        mManualModeAdapter?.setItems(mManualModeEntityList, object: DiffCallback<ManualModeEntity>() {
            override fun areItemsTheSame(oldItem: ManualModeEntity, newItem: ManualModeEntity): Boolean {
                return oldItem.soundModelId == newItem.soundModelId
            }

            override fun areContentsTheSame(oldItem: ManualModeEntity, newItem: ManualModeEntity): Boolean {
                return oldItem.isSwitchSelect == newItem.isSwitchSelect &&
                        oldItem.colorValue == newItem.colorValue &&
                        oldItem.brightnessProgress == newItem.brightnessProgress
            }

            override fun getChangePayload(oldItem: ManualModeEntity, newItem: ManualModeEntity): Any {
                val bundle = Bundle()
                if (oldItem.isSwitchSelect != newItem.isSwitchSelect) {
                    bundle.putBoolean("isChecked", newItem.isSwitchSelect)
                }
                if (oldItem.colorValue != newItem.colorValue) {
                    bundle.putInt("color", newItem.colorValue)
                }
                if (oldItem.brightnessProgress != newItem.brightnessProgress) {
                    bundle.putInt("brightness", newItem.brightnessProgress)
                }
                return bundle
            }
        })
    }

    private fun initManualMode() {
        settingBinding?.includeLighting?.resetBtn?.setOnKeyListener(object: BaseOnKeyListener() {
            override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (isEnterKey(keyCode, event)) {
                    val dialog = AAMFloatingDialog(YKApplication.app,
                        message = YKApplication.app.getString(R.string.light_setting_reset_tips),
                        positiveButtonText = YKApplication.app.getString(R.string.str_confirm),
                        positiveButtonClickListener = { dialog, _ ->
                            onResetLightParams()
                            dialog.dismiss()
                        },
                        negativeButtonText = YKApplication.app.getString(R.string.cancel),
                        negativeButtonClickListener = {
                            it.dismiss()
                        })
                    dialog.onDismiss = {
                        // 重新开始计时，延迟关闭弹窗
                        settingWindowCallback?.startSettingWindowTime()
                    }
                    dialog.show()
                    // 停止延迟关闭弹窗计时
                    settingWindowCallback?.stopSettingWindowTime()
                    return true
                }
                if (isBackKey(keyCode,event)||isMenuKey(keyCode,event)/*|| currentActivity!!.disallowOpenSettingWindow()*/){
                    settingBinding?.includeLighting?.tvManualModeLight?.requestFocus()
                    return true
                }
                return false
            }
        })

        manualModePreInflateHelper.preload(
                settingBinding?.root as android.view.ViewGroup,
        R.layout.item_manual_mode, 3)

        mManualModePresenter = ManualModePresenter(manualModePreInflateHelper!!)
        mManualModeAdapter = ArrayObjectAdapter(mManualModePresenter)
        mManualModePresenter?.run{
            onColorChangeCallback = {
                // 修改颜色的回调
                val receiver = YKApplication.app.machineLightModeChangeReceiver
                val currentModeId = receiver.currentMode
                when (it.soundModelId) {
                    "1" -> {
                        // 屏幕灯
                        receiver.setScreenLightColor(currentModeId, it.colorValue)
                    }
                    "2" -> {
                        // 天花灯
                        receiver.setCeilingLightColor(currentModeId, it.colorValue)
                    }
                    "3" -> {
                        // 氛围灯
                        receiver.setDMXLightColor(currentModeId, it.colorValue)
                    }
                    else -> {}
                }
            }
            onBrightnessChangeCallback = {
                // 修改亮度的回调
                val receiver = YKApplication.app.machineLightModeChangeReceiver
                val currentModeId = receiver.currentMode
                when (it.soundModelId) {
                    "1" -> {
                        // 屏幕灯
                        receiver.setScreenLightBrightness(currentModeId, it.brightnessProgress)
                    }
                    "2" -> {
                        // 天花灯
                        receiver.setCeilingLightBrightness(currentModeId, it.brightnessProgress)
                    }
                    "3" -> {
                        // 氛围灯
                        receiver.setDMXLightBrightness(currentModeId, it.brightnessProgress)
                    }
                    else -> {}
                }

            }
            onSwitchClick = {
                // 修改开关的回调
                val receiver = YKApplication.app.machineLightModeChangeReceiver
                val currentModeId = receiver.currentMode
                when (it.soundModelId) {
                    "1" -> {
                        // 屏幕灯
                        receiver.setScreenLightEnable(currentModeId, it.isSwitchSelect)
                    }
                    "2" -> {
                        // 天花灯
                        receiver.setCeilingLightEnable(currentModeId, it.isSwitchSelect)
                    }
                    "3" -> {
                        // 氛围灯
                        receiver.setDMXLightEnable(currentModeId, it.isSwitchSelect)
                    }
                    else -> {}
                }
            }
//            onItemSelected = {
//                settingWindowCallback?.startSettingWindowTime()
//            }
        }

        val bridgeAdapter = ItemBridgeAdapter(mManualModeAdapter)
        settingBinding?.includeLighting?.rvManualMode?.adapter = bridgeAdapter
        settingBinding?.includeLighting?.rvManualMode?.onDispatchKeyListener = View.OnKeyListener { _, _, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP && keyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                settingBinding?.includeLighting?.tvManualModeLight?.requestFocus()
                true
            } else {
                false
            }
        }
    }



    private fun onResetLightParams() {
        val receiver = YKApplication.app.machineLightModeChangeReceiver
        val currentModeId = receiver.currentMode
        receiver.modeColorMap[currentModeId]?.apply {
            screenLightConfig.color = screenLightDefaultConfig.color
            screenLightConfig.brightness = screenLightDefaultConfig.brightness
            screenLightConfig.isOn = screenLightDefaultConfig.isOn

            ceilingLightConfig.color = ceilingLightDefaultConfig.color
            ceilingLightConfig.brightness = ceilingLightDefaultConfig.brightness
            ceilingLightConfig.isOn = ceilingLightDefaultConfig.isOn

            ambientLightConfig.color = ambientLightDefaultConfig.color
            ambientLightConfig.brightness = ambientLightDefaultConfig.brightness
            ambientLightConfig.isOn = ambientLightDefaultConfig.isOn

            scope.launch {
                receiver.updateConfigToLight(currentModeId)
            }
            refreshManualModeList()
        }
    }

    private fun initLightingScenel() {
        LogUtils.d(TAG, "initLightingScenel")
        //触屏点击
        if (GlobalVariable.isTouchMode) {
            settingBinding?.includeLighting?.tvSmartLightingScenes!!.isSelected = true
            settingBinding?.includeLighting?.tvSmartLightingScenes?.isFocusable = false
            settingBinding?.includeLighting?.tvSmartLightingScenes?.isFocusableInTouchMode = false
            settingBinding?.includeLighting?.tvSmartLightingScenes?.setOnClickListener {
                showSongCountTab(true, false)
                settingBinding?.includeLighting?.tvSmartLightingScenes!!.isSelected = true
                settingBinding?.includeLighting?.tvManualModeLight!!.isSelected = false
            }
            settingBinding?.includeLighting?.tvManualModeLight?.isFocusable = false
            settingBinding?.includeLighting?.tvManualModeLight?.isFocusableInTouchMode = false
            settingBinding?.includeLighting?.tvManualModeLight?.setOnClickListener {
                val currentMode = YKApplication.app.machineLightModeChangeReceiver.currentMode
                when (currentMode) {
                    MachineLightModeChangeReceiver.MODE_1,
                    MachineLightModeChangeReceiver.MODE_1_1,
                    MachineLightModeChangeReceiver.MODE_OFF-> {}
                    else -> {
                        showSongCountTab(false, true)
                        settingBinding?.includeLighting?.tvManualModeLight!!.isSelected = true
                        settingBinding?.includeLighting?.tvSmartLightingScenes!!.isSelected = false
                    }
                }
            }
        }else {
            settingBinding?.includeLighting?.tvSmartLightingScenes!!.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus){
                    showSongCountTab(true, false)
//                    switchView(settingBinding?.includeLighting?.clLightingSceneLayout!!,settingBinding?.includeLighting?.manualModeLightLayout!!,settingBinding?.includeLighting?.arcControlModeLayout!!)
                    settingBinding?.includeLighting?.tvManualModeLight!!.isSelected = false

                    scrollToSelectedItemInSmartMode()

                    if (settingBinding?.includeLighting?.clLightingSceneLayout!!.isVisible){
                        return@setOnFocusChangeListener
                    }
                    slideToRight(settingBinding?.includeLighting?.manualModeLightLayout!!, settingBinding?.includeLighting?.clLightingSceneLayout!!)
                }else{
                    settingBinding?.includeLighting?.tvSmartLightingScenes!!.isSelected = true
                }
            }

            settingBinding?.includeLighting?.tvSmartLightingScenes?.setOnKeyListener(object : BaseOnKeyListener() {
                override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event?.action == KeyEvent.ACTION_DOWN) {
                        settingBinding?.rvSettingTab?.findViewHolderForAdapterPosition(
                            SettingWindow.itemPositionSelect
                        )?.itemView?.requestFocus()
                        return true
                    }
                    if (ispDpadDownKey(keyCode,event)) {
                        settingBinding?.includeLighting?.rvLightingScene?.requestFocus()
                        return true
                    }
                    if (isBackKey(keyCode,event)||isMenuKey(keyCode,event)/*|| currentActivity!!.disallowOpenSettingWindow()*/){
                        settingBinding?.rvSettingTab?.findViewHolderForAdapterPosition(SettingWindow.itemPositionSelect)?.itemView?.requestFocus()
                        return true
                    }
                    return false
                }
            })

            settingBinding?.includeLighting?.tvManualModeLight?.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus){
                    showSongCountTab(false, true)
                    settingBinding?.includeLighting?.tvSmartLightingScenes!!.isSelected = false
                    val machineLightReceiver = YKApplication.app.machineLightModeChangeReceiver
                    val currentLightMode = machineLightReceiver.currentMode
                    machineLightReceiver.getKTVLightSceneModeList().find {
                        it.id == currentLightMode
                    }?.let {
                        settingBinding?.includeLighting?.modeNameTv?.text = it.scenarioName
                    }
                    when (currentLightMode) {
                        MachineLightModeChangeReceiver.MODE_OFF,
                        MachineLightModeChangeReceiver.MODE_1,
                        MachineLightModeChangeReceiver.MODE_1_1-> {
                            settingBinding?.includeLighting?.resetBtn?.let {
                                it.nextFocusDownId = it.id
                            }
                        }
                        else -> {
                            val rvManualMode = settingBinding?.includeLighting?.rvManualMode
                            if (rvManualMode != null) {
                                settingBinding?.includeLighting?.resetBtn?.let {
                                    it.nextFocusDownId = rvManualMode.id
                                }
                            }

                            // 刷新列表
                            if (mManualModeAdapter?.size() == 0) {
                                initManualModeListData()
                            } else {
                                refreshManualModeList()
                            }
                        }
                    }
                    if (settingBinding?.includeLighting?.manualModeLightLayout!!.isVisible){
                        return@setOnFocusChangeListener
                    }
                    if (settingBinding?.includeLighting?.clLightingSceneLayout?.visibility == View.VISIBLE){
                        slideToLeft(settingBinding?.includeLighting?.clLightingSceneLayout!!,
                            settingBinding?.includeLighting?.manualModeLightLayout!!)
                    }
                }else{
                    settingBinding?.includeLighting?.tvManualModeLight!!.isSelected = true
                }
            }

            settingBinding?.includeLighting?.tvManualModeLight?.setOnKeyListener(object : BaseOnKeyListener() {
                override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event?.action == KeyEvent.ACTION_DOWN) {
                        settingBinding?.rvSettingTab?.findViewHolderForAdapterPosition(
                            SettingWindow.itemPositionSelect
                        )?.itemView?.requestFocus()
                        return true
                    }
                    if (isBackKey(keyCode,event)||isMenuKey(keyCode,event)/*|| currentActivity!!.disallowOpenSettingWindow()*/){
                        settingBinding?.rvSettingTab?.findViewHolderForAdapterPosition(SettingWindow.itemPositionSelect)?.itemView?.requestFocus()
                        return true
                    }
                    return false
                }
            })
        }

        val currentLightMode = YKApplication.app.machineLightModeChangeReceiver.currentMode
        mSmartScenesEntityList.clear()
        mSmartScenesEntityList.addAll(YKApplication.app.machineLightModeChangeReceiver.getKTVLightSceneModeList())

        mSmartScenesEntityList.find {
            it.id == currentLightMode
        }?.let {
            it.select = true
        }

        lightingScenelHelper.preload(
            settingBinding?.root as android.view.ViewGroup,
            R.layout.item_lighting_scenel, mSmartScenesEntityList.size)

        mLightingScenelPresenter = LightingScenelPresenter(scope,lightingScenelHelper!!)
        mLightingScenelAdapter = ArrayObjectAdapter(mLightingScenelPresenter)

        mLightingScenelPresenter?.run {
//            recyclerView = settingBinding?.includeLighting?.rvLightingScene!!
            onItemUpKeyCallback = {
//                LogUtils.i(TAG,"------智能灯光场景-获取焦点")
                settingBinding?.includeLighting?.tvSmartLightingScenes?.requestFocus()
            }
            onItemBackKeyCallback = {
//                LogUtils.i(TAG,"------智能灯光场景-获取焦点")
                settingBinding?.includeLighting?.tvSmartLightingScenes?.requestFocus()
            }
            onItemSelected = { smartScenesEntity ->
                val lastSelectedItem = mSmartScenesEntityList.find { it.select }
                if (lastSelectedItem != smartScenesEntity) {
                    scope.launch(Dispatchers.Main){
                        try {
                            val result = DeviceCtlManager.exec<Any>(
                                DeviceCtlCmd.SET_LIGHT_SCENE, mutableMapOf(
                                    "scene_id" to smartScenesEntity.id.toString()
                                )
                            )
                            LogUtils.i(TAG, "DeviceCtlManager SET_LIGHT_SCENE scene_id:" + smartScenesEntity.id +
                                " result:" + GsonUtils.toJson(result))
                            if (result.code != 0) {
                                ToastUtil.show(YKApplication.app.getString(R.string.switch_failure))
                            } else {
                                // 设置成功，在灯光模式回调中更新UI
//                                val lastIndex = if (lastSelectedItem == null) {
//                                    -1
//                                } else {
//                                    mSmartScenesEntityList.indexOf(lastSelectedItem)
//                                }
//
//                                // 本次选中的位置
//                                val currentIndex = mSmartScenesEntityList.indexOf(smartScenesEntity)
//                                if (lastIndex >= 0) {
//                                    mLightingScenelAdapter?.notifyItemRangeChanged(lastIndex, 1,
//                                        Bundle().apply {
//                                        putBoolean("isSelected", false)
//                                    })
//                                }
//
//                                mLightingScenelAdapter?.notifyItemRangeChanged(currentIndex,1, Bundle().apply {
//                                    putBoolean("isSelected", true)
//                                })

                            }
                        } catch (e: Exception) {
                            LogUtils.e(TAG, "", e)
                            ToastUtil.show(YKApplication.app.getString(R.string.switch_failure))
                        }
                    }
                    LogUtils.i(TAG,"set light scene mode:${smartScenesEntity.scenarioName}, ${smartScenesEntity.type}")
                } else {
                    ToastUtil.show(YKApplication.app.getString(R.string.scenario_light_mode_same))
                }
            }
        }

        val bridgeAdapter = ItemBridgeAdapter(mLightingScenelAdapter)
        settingBinding?.includeLighting?.rvLightingScene?.adapter = bridgeAdapter
//        settingBinding?.includeLighting?.rvLightingScene?.layoutManager = GridLayoutManager(appContext, 6)
//        settingBinding?.includeLighting?.rvLightingScene?.setNumRows(6)
        scope.launch(Dispatchers.Main) {
            mLightingScenelAdapter?.clear()
            mLightingScenelAdapter?.addAll(0, mSmartScenesEntityList)
        }
    }

    private fun refreshManualModeList() {
//        val machineLightReceiver = YKApplication.app.machineLightModeChangeReceiver
//        val currentLightMode = machineLightReceiver.currentMode
//        YKApplication.app.machineLightModeChangeReceiver.modeColorMap[currentLightMode]?.let { config ->
//            // 屏幕灯
//            mManualModeAdapter?.notifyItemRangeChanged(0, 1, Bundle().apply {
//                putInt("color", config.screenLightConfig.color)
//                putInt("brightness", config.screenLightConfig.brightness)
//                putBoolean("isChecked", config.screenLightConfig.isOn)
//            })
//            // 天花灯
//            mManualModeAdapter?.notifyItemRangeChanged(1, 1, Bundle().apply {
//                putInt("color", config.ceilingLightConfig.color)
//                putInt("brightness", config.ceilingLightConfig.brightness)
//                putBoolean("isChecked", config.ceilingLightConfig.isOn)
//            })
//            // 氛围灯
//            mManualModeAdapter?.notifyItemRangeChanged(2, 1, Bundle().apply {
//                putInt("color", config.ambientLightConfig.color)
//                putInt("brightness", config.ambientLightConfig.brightness)
//                putBoolean("isChecked", config.ambientLightConfig.isOn)
//            })
//        }
        initManualModeListData()
    }

    /**
     * 智能场景模式列表优化，默认滑动到选择的位置，让用户看到当前的模式
     */
    private fun scrollToSelectedItemInSmartMode() {
        val selectedSmartModePosition = mSmartScenesEntityList.find {
            it.select
        }?.let {
            mSmartScenesEntityList.indexOf(it)
        }?: -1
        if (selectedSmartModePosition >= 0) {
            settingBinding?.includeLighting?.rvLightingScene?.smoothScrollToPosition(selectedSmartModePosition)
        }
    }

    private fun showSongCountTab(
        smartLightingScenesVisible: Boolean,
        manualModeLightVisible: Boolean,
    ) {
//        settingBinding?.includeLighting?.clLightingSceneLayout?.isVisible = smartLightingScenesVisible
//        settingBinding?.includeLighting?.manualModeLightLayout?.isVisible = manualModeLightVisible
//        settingBinding?.includeLighting?.arcControlModeLayout?.isVisible = arcContorlVisible

        settingBinding?.includeLighting?.viewSmartLightingScenesLine?.setBackgroundColor(if(smartLightingScenesVisible) appContext.getColor(R.color.white) else appContext.getColor(R.color.transparent) )
        settingBinding?.includeLighting?.viewManualModeLightLine?.setBackgroundColor(if(manualModeLightVisible) appContext.getColor(R.color.white) else appContext.getColor(R.color.transparent) )
//        settingWindowCallback?.startSettingWindowTime()
    }

    private fun slideToLeft(currentView: View, nextView: View) {
        // 隐藏当前布局
        currentView.visibility = View.GONE
        currentView.clearAnimation()  // 清除动画，释放资源

        // 加载左滑进入动画
        val slideInLeft = AnimationUtils.loadAnimation(currentView.context, R.anim.lighting_btn_slide_in_left)

        // 设置动画监听器
        slideInLeft.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // 动画结束，清除动画和监听器，释放内存
                nextView.clearAnimation()
                slideInLeft.setAnimationListener(null)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        nextView.startAnimation(slideInLeft)
        nextView.visibility = View.VISIBLE  // 显示下一个布局
    }

    private fun slideToRight(currentView: View, previousView: View) {
        // 隐藏当前布局
        currentView.visibility = View.GONE
        currentView.clearAnimation()  // 清除动画，释放资源

        // 加载右滑进入动画
        val slideInRight = AnimationUtils.loadAnimation(currentView.context, R.anim.lighting_btn_slide_in_right)

        // 设置动画监听器
        slideInRight.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // 动画结束，清除动画和监听器，释放内存
                previousView.clearAnimation()
                slideInRight.setAnimationListener(null)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        previousView.startAnimation(slideInRight)
        previousView.visibility = View.VISIBLE  // 显示前一个布局
    }
}