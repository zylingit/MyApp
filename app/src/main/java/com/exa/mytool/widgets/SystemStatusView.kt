package com.aam.mida.mida_yk.widgets

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalBroadcastAction
import com.aam.mida.base.GlobalVariable
import com.aam.mida.base.SPConstant
import com.aam.mida.base.anim.BaseOnKeyListener
import com.aam.mida.base.base.BaseActivity
import com.aam.mida.base.ext.currentActivity
import com.aam.mida.base.ext.killApp
import com.aam.mida.base.utils.AniUtils
import com.aam.mida.base.utils.WifiUtils
import com.aam.mida.base.widgets.AAMDialog
import com.aam.mida.base.widgets.ScaleConstraintLayout
import com.aam.mida.mida_yk.R
import com.aam.mida.mida_yk.YKApplication
import com.aam.mida.mida_yk.activity.GlobeSettingsActivity
import com.aam.mida.mida_yk.activity.QuickSettingsActivity
import com.aam.mida.mida_yk.activity.StandbyActivity
import com.aam.mida.mida_yk.activity.StandbyOrderActivity
import com.aam.mida.mida_yk.blemic.BleMicManager
import com.aam.mida.mida_yk.blemic.BleMicManagerCallback
import com.aam.mida.mida_yk.constant.AAMServiceManager
import com.aam.mida.mida_yk.databinding.LayoutSystemStatusViewBinding
import com.aam.mida.mida_yk.dialog.AirConditionSettingDialog
import com.aam.mida.mida_yk.dialog.MicrophoneHintDialog
import com.aam.mida.mida_yk.dialog.SettingCheckPasswordDialog
import com.aam.mida.mida_yk.entity.MicrophoneBatteryEntity
import com.aam.mida.mida_yk.observer.AppbarStateObserable
import com.aam.mida.mida_yk.panel.DisplayPanelManager
import com.aam.mida.mida_yk.service.SettingWindow
import com.aam.soundsetting.ProductType
import com.android.internal.app.LocalePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale


/**
 * @author          pgl
 * @time            2023/1/3 14:57
 * @des             系统状态View WIFI状态/时间
 *
 * @version         1.0v$
 * @updateAuthor    $
 * @updateDate      $
 * @updateDes       系统状态View WIFI状态/时间
 */
class SystemStatusView : FrameLayout, BleMicManagerCallback {


    private var viewBinding: LayoutSystemStatusViewBinding? = null

    private var scope: CoroutineScope? = null
    private var mLevel: Int = 0

    private var speed: String = ""
    private var speedUnit: String = ""

    private  var mMicrophoneHintDialog: MicrophoneHintDialog? = null
    var isMicrophoneConnected = false
    var mMicrophoneConnectedCount = 0


    companion object{
        private val TAG = SystemStatusView::class.java.simpleName
    }

    private val mWifiIconList = mutableListOf<Int>(
        R.drawable.ic_wifi_signal_4,
        R.drawable.ic_wifi_signal_3,
        R.drawable.ic_wifi_signal_2,
        R.drawable.ic_wifi_signal_1,
        R.drawable.ic_wifi_signal_1
    )

    private val dateTimeRunnable = DateTimeRunnable()


    private var mAirConditionSettingDialog: AirConditionSettingDialog? = null


    private val mMicrophoneList = mutableListOf<MicrophoneBatteryEntity>()
//    private var mMicrophoneBatteryAdapter: MicrophoneBatteryAdapter? = null

    private var monthCalendarWindow: MonthCalendarWindow? = null

    private var micInfoWindow: MicInfoWindow? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
    }

    private val dataTimeCallback: (pmAm: String, dayOfWeek: String, hhMM: String) -> Unit =
        { pmAm, dayOfWeek, hhMM ->
            updateDateTime(pmAm, dayOfWeek, hhMM)
        }

    private val wifiCallback: (signalLevle: Int) -> Unit = { level ->
        mLevel = level
        updateWifiSingal(level,0)
    }

    private val headsetCallback: (isConnected: Boolean) -> Unit = { status ->
        updateHeadset(status)
    }

    private val networkSpeedCallback: (networkSpeed: String, speedUnit: String) -> Unit = { speed, speedUnit ->
        updateNetworkSpeed(speed, speedUnit)
    }

    private val networkTypeChangeCallback: (Int) -> Unit = { type ->
        when (type) {
            0 -> {
                // wlan
                updateWifiSingal(0,0)
            }
            1 -> {
                // eth
                updateWifiSingal(-2,1)
            }
            -1 -> {
                // 未连接
                updateWifiSingal(-1,1)
            }
        }
    }

    private val onChannelVolumeSettingChangeCallback: (isVisible: Boolean) -> Unit = { isVisible ->
        if (isMoreBtnOrWifiBtnLayoutVisible) {
            viewBinding?.channelVolumeSettingBtnLayout?.isVisible = isVisible
        }
    }

    /**
     * 麦克风电池信息
     */
    private val micBatteryInfoMap = mutableMapOf<String,Int>()

    /**
     * 麦克风按键点击提示相关
     */
    private var mic1BtnJob: Job? = null

    /**
     * 麦克风按键点击提示相关
     */
    private var mic2BtnJob: Job? = null

    /**
     * 更多按钮、WIFI图标是否可见
     */
    private var isMoreBtnOrWifiBtnLayoutVisible = true


    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewBinding =
            DataBindingUtil.inflate(inflater, R.layout.layout_system_status_view, this, false)
        addView(viewBinding?.root)

        initListener()

        initMicBattery()
    }

    fun initListener(){
        //TV版更多按钮与网络图标，获取焦点，放大缩小
        if (GlobalVariable.isTouchMode){
            viewBinding?.moreBtnLayout?.isFocusable = false
            viewBinding?.airConditionBtnLayout?.isFocusable = false
            viewBinding?.wifiBtnLayout?.isFocusable = false
            viewBinding?.settingBtnLayout?.isFocusable = false
            viewBinding?.timeLayout?.isFocusable = false
            viewBinding?.switchLanguageBtnLayout?.isFocusable = false
        }

        isMicrophoneConnected = SPConstant.isMicrophoneConnected(context)
        mMicrophoneConnectedCount = SPConstant.getMicrophoneConnectedCount(context)
        viewBinding?.wifiBtnLayout?.setonViewClick setOnClickListener@{
            if ((currentActivity is StandbyOrderActivity)||(currentActivity is StandbyActivity))return@setOnClickListener
            LogUtils.i(TAG,"---------------打开网络链接--")
            GlobeSettingsActivity.startLaunch(YKApplication.app, SettingWindow.SETTING_TAB_NETWORK,false)
        }

        viewBinding?.moreBtnLayout?.setonViewClick setOnClickListener@{
            LogUtils.i(TAG,"---------------打开更多--")
            if ((currentActivity is StandbyOrderActivity)||(currentActivity is StandbyActivity))return@setOnClickListener
            YKApplication.app.openSettingBroadcast()
        }

//        viewBinding?.timeLayout?.setonViewClick animScaleView@{
//            LogUtils.i(TAG,"---------------打开日期显示框--")
//            if ((currentActivity is StandbyOrderActivity)||(currentActivity is StandbyActivity))return@animScaleView
//    //            viewBinding.monthsCalendarLayout.isVisible = true
//    //            showMonthCalendarWindow(viewBinding.timeLayout)
//            if (monthCalendarWindow != null){
//                dismissMonthCalendarWindow()
//            } else {
//                viewBinding?.timeLayout?.let { showMonthCalendarWindow(it) }
//            }
//            viewBinding?.rootDialogLayout?.isVisible = true
//        }

        viewBinding?.settingBtnLayout?.setonViewClick setOnClickListener@{
            LogUtils.i(TAG,"---------------打开后台设置界面--")
            if ((currentActivity is StandbyOrderActivity)||(currentActivity is StandbyActivity))return@setOnClickListener
            onSettingMenuClick()
        }

        viewBinding?.rootDialogLayout?.setonViewClick {
            LogUtils.i(TAG,"---------------打开后台设置界面--")
//            viewBinding.monthsCalendarLayout.isVisible = false
            dismissMonthCalendarWindow()
            viewBinding?.rootDialogLayout?.isVisible = false
//            viewBinding.microphoneLayout.isVisible = false
            dismissMicInfoWindow()
        }

        viewBinding?.timeLayout?.focusChangeListener = {
            if (it.not()) {
                dismissMonthCalendarWindow()
            }
        }

        viewBinding?.timeLayout?.setOnKeyListener(object : BaseOnKeyListener() {
            override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                v ?: return false
                return when {
                    isBackKey(keyCode, event) -> {
                        if (monthCalendarWindow?.isShowing == true){
//                            viewBinding.monthsCalendarLayout.isVisible = false
                            dismissMonthCalendarWindow()
                            viewBinding?.rootDialogLayout?.isVisible = false
                            true
                        }else{
                            false
                        }
                    }
                    isEnterKey(keyCode, event) -> {
//                        viewBinding.monthsCalendarLayout.isVisible = true
                        if (monthCalendarWindow != null){
                            dismissMonthCalendarWindow()
                        } else {
                            viewBinding?.timeLayout?.let { showMonthCalendarWindow(it) }
                        }
                        viewBinding?.rootDialogLayout?.isVisible = false
                        true
                    }
//                    monthCalendarWindow?.isShowing == true -> {
////                        viewBinding.monthsCalendarLayout.isVisible = false
//                        dismissMonthCalendarWindow()
//                        viewBinding.rootDialogLayout.isVisible = false
//                        true
//                    }
                    else -> false
                }
            }
        })

        viewBinding?.timeLayout?.let {
            AniUtils.animScaleView(it) {
                if (monthCalendarWindow != null){
                    dismissMonthCalendarWindow()
                } else {
                    viewBinding?.timeLayout?.let { showMonthCalendarWindow(it) }
                }
                viewBinding?.rootDialogLayout?.isVisible = false
            }
        }

//        viewBinding?.wifiBtnLayout?.setOnKeyListener(object : BaseOnKeyListener() {
//            override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
//                v ?: return false
//                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event?.action == KeyEvent.ACTION_DOWN && (currentActivity is MainActivity)) {
//                    LogUtils.i(TAG,"-------------WIFI状态栏按钮--上移动--")
////                    YKApplication.app.openSettingBroadcast()
//                }
//                return false
//            }
//        })

//        viewBinding?.moreBtnLayout?.setOnKeyListener(object : BaseOnKeyListener() {
//            override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
//                v ?: return false
//                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event?.action == KeyEvent.ACTION_DOWN && (currentActivity is MainActivity)) {
//                    LogUtils.i(TAG,"-------------更多状态栏按钮--上移动--")
////                    YKApplication.app.openSettingBroadcast()
//                }
//                return false
//            }
//        })


        //2240商用版本（影K吧）---- 保留旧版本空调快捷操作入口，隐藏其他全局设置内容
        if ((GlobalVariable.produceType == ProductType.HS68_2240 ||
                GlobalVariable.produceType == ProductType.HS68_2242 ||
                GlobalVariable.produceType == ProductType.HS68_2703 ||
                GlobalVariable.produceType == ProductType.HS68_2704) &&
            GlobalVariable.machineType == 2) {
            viewBinding?.moreBtnLayout?.isVisible = false
            viewBinding?.airConditionBtnLayout?.isVisible = true
            viewBinding?.airConditionBtnLayout?.setonViewClick {
                //showAirConditionSettingDialog()
                LocalBroadcastManager.getInstance(context).sendBroadcast(
                    Intent(GlobalBroadcastAction.ACTION_OPEN_AIR_DIALOG))
            }
        }else{
            viewBinding?.moreBtnLayout?.isVisible = true
            viewBinding?.airConditionBtnLayout?.isVisible = false
        }

        viewBinding?.switchLanguageBtnLayout?.setOnKeyListener(object: BaseOnKeyListener() {
            override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (isEnterKey(keyCode, event)) {
                    showSwitchLanguageDialog()
                    return true
                }

                return false
            }

        })

        viewBinding?.switchLanguageBtnLayout?.setonViewClick {
            showSwitchLanguageDialog()
        }

        if (isMoreBtnOrWifiBtnLayoutVisible) {
            // 暂不设置触屏版入口
            if (GlobalVariable.isTouchMode) {
                viewBinding?.channelVolumeSettingBtnLayout?.isVisible = false
            } else {
                if (ProductType.isSupportedChannelVolumeSetting(GlobalVariable.produceType).not()) {
                    // 不支持多声道音量设置
                    viewBinding?.channelVolumeSettingBtnLayout?.isVisible = false
                } else  {
                    viewBinding?.channelVolumeSettingBtnLayout?.isVisible = SPConstant.isKTVTuningVisible(context)
                }

                viewBinding?.channelVolumeSettingBtnLayout?.setOnKeyListener(object: BaseOnKeyListener() {
                    override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                        if (isEnterKey(keyCode, event)) {
                            AAMServiceManager.openGlobalTuningSoundSetting(GlobalVariable.isTouchMode)
                            return true
                        }

                        return false
                    }

                })
            }
        }

    }

    private fun View.setonViewClick(callback: () -> Unit) {
        if (GlobalVariable.isTouchMode) {
            AniUtils.animScaleView(this) {
                callback.invoke()
            }
        } else {
            setOnKeyListener(object: BaseOnKeyListener() {
                override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (isEnterKey(keyCode, event)) {
                        callback.invoke()
                        return true
                    }

                    return false
                }

            })
        }
    }

    /**
     * 切换语音弹窗
     */
    private fun showSwitchLanguageDialog() {
        val title = context.getString(R.string.language_dialog_title)
        val usedStr = context.getString(R.string.language_used)

        val currentLocale = resources.configuration.locales[0]
        val dataList = mutableListOf<String>()
        var defaultSelected = -1

        GlobalVariable.SUPPORTED_LANGUAGE.forEachIndexed { index, locale ->
            dataList.add(if (locale.language == currentLocale.language) {
                defaultSelected = index
                "${locale.getDisplayName(locale)}（$usedStr）"
            } else {
                locale.getDisplayName(locale)
            })
        }
        val dialog = AAMDialog.create(title = title,
            isTouchMode = GlobalVariable.isTouchMode,
            singleChoiceItems = dataList,
            singleChoiceDefaultCheckItem = if (defaultSelected == -1) null else defaultSelected,
            singleChoiceListener = {
                if (it != defaultSelected) {
                    showSwitchLanguageConfirmDialog(GlobalVariable.SUPPORTED_LANGUAGE[it])
                }
            })

        (context as? BaseActivity)?.let {
            dialog.show(it.supportFragmentManager, "AAMDialog")
        }
    }

    /**
     * 切换语音弹窗（二次确认）
     */
    private fun showSwitchLanguageConfirmDialog(language: Locale) {
        val dialog = AAMDialog.create(message = context.getString(R.string.language_switch_confirm,
            language.displayName),
            positiveButtonText = context.getString(R.string.ok),
            isTouchMode = GlobalVariable.isTouchMode,
            positiveButtonClickListener = { _, _ ->
                LogUtils.d(TAG, "切换系统语言 => ${language.displayName}")
                LocalePicker.updateLocale(language)
                killApp(context, context.packageName)
            },
            negativeButtonText = context.getString(R.string.str_cancel))

        (context as? BaseActivity)?.let {
            dialog.show(it.supportFragmentManager, "AAMDialog")
        }
    }

    fun setSwitchLanguageButtonVisible(visible: Boolean) {
        viewBinding?.switchLanguageBtnLayout?.visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setOnFocusListener(view: ConstraintLayout){
        view.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus){
                // 获取焦点时启动放大动画
                view.scaleX = 1.2F
                view.scaleY = 1.2F
            }else {
                // 失去焦点时恢复原始大小
                view.scaleX = 1.0F
                view.scaleY = 1.0F
            }
        }
    }


    private fun initMicBattery() {
//        mMicrophoneBatteryAdapter = MicrophoneBatteryAdapter(mMicrophoneList)
//
//        viewBinding.rvMicrophoneBattery.run {
//            adapter = mMicrophoneBatteryAdapter
//            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        }
//        mMicrophoneBatteryAdapter?.run {
//            recyclerView = viewBinding.rvMicrophoneBattery
//        }
    }

    private fun showMonthCalendarWindow(view: View) {
        dismissMonthCalendarWindow()
        monthCalendarWindow = MonthCalendarWindow(context)
        monthCalendarWindow?.setOnDismissListener {
            monthCalendarWindow = null
        }
        monthCalendarWindow?.showAsDropDown(view)
    }

    private fun dismissMonthCalendarWindow() {
        monthCalendarWindow?.dismiss()
        monthCalendarWindow = null
    }

    private fun showMicInfoWindow(view: View) {
        dismissMicInfoWindow()
        micInfoWindow = MicInfoWindow(context)
        micInfoWindow?.setOnDismissListener {
            micInfoWindow = null
        }

        micInfoWindow?.showAsDropDown(view)
    }

    private fun dismissMicInfoWindow() {
        micInfoWindow?.dismiss()
        micInfoWindow = null
    }

    /**
     * 隐藏设置与WIFI按钮，即只显示
     */
    fun setMoreBtnOrWifiBtnLayoutVisible(isVisible: Boolean){
        isMoreBtnOrWifiBtnLayoutVisible = isVisible
    }

    private fun updateMicInfo(micList: List<Pair<String, Int>>) {
        // 根据麦克风列表是否为空，设置布局可见性
        setMicBtnVisibility(micList.isNotEmpty())

//        var childCount = viewBinding.micInfoLayout.childCount

//        LogUtils.i(TAG,"--------------childCount:$childCount")
        LogUtils.i(TAG,"--------------micList.size：${micList.size}")

        // 调整子视图数量
//        adjustChildViews(childCount, micList.size)

        // 更新麦克风信息
//        updateMicInfoViews(micList)

        // 更新提示
//        updateMicTitleHint(micList)
    }

    private fun setMicBtnVisibility(visibility: Boolean) {
        if (visibility) {
            if (viewBinding?.micBtnLayout?.isInflated?.not() == true) {
                viewBinding?.micBtnLayout?.viewStub?.inflate()
            }
            viewBinding?.micBtnLayout?.root?.visibility = View.VISIBLE

            AniUtils.animScaleView(viewBinding?.micBtnLayout?.root) {
                if ((currentActivity is StandbyActivity))return@animScaleView
                LogUtils.i(TAG,"---------------打开网络链接--")
//            viewBinding.microphoneLayout.isVisible = true
                viewBinding?.micBtnLayout?.root?.let { showMicInfoWindow(it) }
                viewBinding?.rootDialogLayout?.isVisible = true
//            GlobeSettingsActivity.startLaunch(YKApplication.app, SettingWindow.SETTING_TAB_MICROPHONE,false)
            }

            (viewBinding?.micBtnLayout?.root as? ScaleConstraintLayout)?.focusChangeListener = {
                if (it.not()) {
                    dismissMicInfoWindow()
                }
            }

            viewBinding?.micBtnLayout?.root?.setOnKeyListener(object : BaseOnKeyListener() {
                override fun onHandleKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    v ?: return false
                    return when {
                        isBackKey(keyCode, event) -> {
//                        viewBinding.microphoneLayout.isVisible = false
                            dismissMicInfoWindow()
                            viewBinding?.rootDialogLayout?.isVisible = false
                            false
                        }
                        isEnterKey(keyCode, event) -> {
//                        viewBinding.microphoneLayout.isVisible = true
                            if (micInfoWindow?.isShowing == true) {
                                dismissMicInfoWindow()
                            } else {
                                viewBinding?.micBtnLayout?.root?.let { showMicInfoWindow(it) }
                            }
                            true
                        }
//                    viewBinding.microphoneLayout.isVisible -> {
//                        viewBinding.microphoneLayout.isVisible = false
//                        viewBinding.rootDialogLayout.isVisible = false
//                        true
//                    }
                        else -> false
                    }
                }
            })

            (viewBinding?.micBtnLayout?.root as? ScaleConstraintLayout)?.isFocusable = GlobalVariable.isTouchMode.not()
        } else {
            if (viewBinding?.micBtnLayout?.isInflated == true) {
                viewBinding?.micBtnLayout?.root?.visibility = View.GONE
            }
        }
    }

    /**
     * 显示链接成功提示
     */
    private fun showMicrophoneHintDialog() {
        mMicrophoneHintDialog = MicrophoneHintDialog(context,mMicrophoneList)
        mMicrophoneHintDialog?.run {
            onDismiss = {
                onDismiss = null
                mMicrophoneHintDialog = null
            }
        }
        mMicrophoneHintDialog?.show()
    }

    private fun updateDateTime(pmAm: String, dayOfWeek: String, hhMM: String) {
        viewBinding?.root?.removeCallbacks(dateTimeRunnable)
        dateTimeRunnable.dayOfWeek = dayOfWeek
        dateTimeRunnable.timeStr = hhMM
        viewBinding?.root?.post(dateTimeRunnable)
    }

    /**
     * type：0代表WIFI,1代表有线
     */
    private fun updateWifiSingal(level: Int,type: Int) {
        LogUtils.i(TAG,"---------------WIFI-mLevel:$mLevel-----type：$type")
        viewBinding?.root?.post {
            if (level == -2) {
                viewBinding?.wifiSignalIntensity?.setImageResource(R.drawable.icon_network_eth_white)
            } else if (level == -1) {
                viewBinding?.wifiSignalIntensity?.setImageResource(R.drawable.icon_network_setup_limit)
            } else {
                if (type == 0) {
                    viewBinding?.wifiSignalIntensity?.setImageResource(mWifiIconList[mLevel])
                }
            }
            viewBinding?.wifiSignalIntensity?.visibility = View.VISIBLE
        }

    }

    private fun updateHeadset(isConnected: Boolean) {
//        viewBinding.root.post {
//            viewBinding.headsetStatus.visibility = if (isConnected && (currentActivity !is StandbyOrderActivity) && (currentActivity !is StandbyActivity)) {
//                View.VISIBLE
//            } else {
//                View.INVISIBLE
//            }
//        }
    }

    private fun updateNetworkSpeed (speed: String, speedUnit: String) {
        this.speed = speed
        this.speedUnit = speedUnit
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (viewBinding == null) {
            initView()
        }

        if (isMoreBtnOrWifiBtnLayoutVisible.not()) {
            viewBinding?.moreBtnLayout?.isVisible = false
            viewBinding?.wifiBtnLayout?.isVisible = false
            viewBinding?.settingBtnLayout?.isVisible = false
            viewBinding?.channelVolumeSettingBtnLayout?.isVisible = false
        }

        viewBinding?.wifiBtnLayout?.isVisible = SPConstant.getNetworkManager(context)

        scope = CoroutineScope(Dispatchers.Main)

        AppbarStateObserable.registerHeadsetStateObserver(this.headsetCallback)
        AppbarStateObserable.registerWifiStateObserver(this.wifiCallback)
        AppbarStateObserable.registerDateTimeStateObserver(this.dataTimeCallback)
        AppbarStateObserable.registerNetworkSpeedObserver(this.networkSpeedCallback)
        AppbarStateObserable.registerNetworkTypeChangeObserver(this.networkTypeChangeCallback)
        AppbarStateObserable.registerChannelVolumeSettingChangeObserver(this.onChannelVolumeSettingChangeCallback)


//        MicrophoneCacheInfoManager.addOnMicBatteryChangeCallback(micBatteryInfoChangeCallback)

        // 手动更新一次网络类型
        networkTypeChangeCallback.invoke(WifiUtils.getNetworkType())
        initBattery()
//        // 手动更新一次麦克风信息
//        micBatteryInfoChangeCallback.invoke(MicrophoneCacheInfoManager.getCacheMicListBatteryInfo())
    }

    private fun initBattery() {
        BleMicManager.addCallback(this)
        val onlineMicList = BleMicManager.getAllConnectedMicMac()
        for (mic in onlineMicList) {
            micBatteryInfoMap[mic] = 0
        }
        BleMicManager.refreshAllOnlineMicInfo()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope?.cancel()

        AppbarStateObserable.unRegisterHeadsetStateObserver(this.headsetCallback)
        AppbarStateObserable.unRegisterWifiStateObserver(this.wifiCallback)
        AppbarStateObserable.unRegisterDateTimeStateObserver(this.dataTimeCallback)
        AppbarStateObserable.unRegisterNetworkSpeedObserver(this.networkSpeedCallback)
        AppbarStateObserable.unregisterNetworkTypeChangeObserver(this.networkTypeChangeCallback)
        AppbarStateObserable.unRegisterChannelVolumeSettingChangeObserver(this.onChannelVolumeSettingChangeCallback)
//        MicrophoneCacheInfoManager.removeOnMicBatteryChangeCallback(micBatteryInfoChangeCallback)
        BleMicManager.removeCallback(this)

        dismissMonthCalendarWindow()
        mAirConditionSettingDialog?.dismiss()
    }

    private fun onSettingMenuClick() {
        SettingCheckPasswordDialog(context, true, object : SettingCheckPasswordDialog.OnActivityIntentListener {
                override fun onConfirmListener() {
                    QuickSettingsActivity.startLaunch(context)
                }
            }
        ).show()
    }

    ///////////////////////////////////////////////////////////////////////////
    // 内部类
    ///////////////////////////////////////////////////////////////////////////

    inner class DateTimeRunnable(var dayOfWeek: String = "", var timeStr: String = ""): Runnable {
        override fun run() {
            viewBinding?.tvDayOfWeek?.text = dayOfWeek
            viewBinding?.tvCurrentTime?.text = timeStr

            if ((currentActivity is StandbyOrderActivity)||(currentActivity is StandbyActivity)) {
                DisplayPanelManager.reqDisplayPanelSetUiWeather2()
            }
        }

    }

    override fun onBlueMicStatusChangeCallback(
        module: String,
        direction: String,
        address: String?,
        state: Int,
        reason: String,
        isDelete: Boolean
    ) {
        if(state == BluetoothGatt.STATE_DISCONNECTED){
            if(!TextUtils.isEmpty(address)) {
                synchronized(micBatteryInfoMap) {
                    if (micBatteryInfoMap.contains(address)) {
                        micBatteryInfoMap.remove(address)
                        notifyBatteryInfo()
                    }
                }

            }
        }

    }

    override fun onBlueMicBatteryChangeCallback(
        module: String,
        direction: String,
        address: String?,
        battery: Int
    ) {
        if(!TextUtils.isEmpty(address)) {
            LogUtils.i(TAG, "battery chang in system status view")
            synchronized(micBatteryInfoMap) {
                micBatteryInfoMap[address!!] = battery
            }
            notifyBatteryInfo()

        }
    }

    override fun onBlueMicRssiChangeCallback(
        module: String,
        direction: String,
        address: String?,
        rssi: Int
    ) {
    }

    private fun notifyBatteryInfo() {
        val batteryList = mutableListOf<Pair<String,Int>>()
        synchronized(micBatteryInfoMap){
            for(kv in micBatteryInfoMap){
                batteryList.add(Pair(kv.key,kv.value))
            }
        }
        scope?.launch {
            try {
                updateMicInfo(batteryList)
            } catch (e: Exception) {
                LogUtils.e(TAG, e)
            }
        }
    }

    override fun onBlueMicFreqPointChangeCallback(
        module: String,
        direction: String,
        address: String?,
        channel: Int,
        roomId: Short?
    ) {

    }

    override fun onBlueMicVersionChangeCallback(
        module: String,
        direction: String,
        address: String?,
        version: String
    ) {

    }

    override fun onInfraredMicNotifyCallback(
        module: String,
        direction: String,
        address: String?,
        battery: Int,
        channel: Int
    ) {

    }

    override fun onBlueMicKeyPressCallback(
        module: String,
        direction: String,
        address: String?,
        keyStatus: Int
    ) {
        //按键按下消息
    }

    override fun onUendModuleCallback(mModule: String, alive: Boolean) {

    }

    override fun onUendVersionCallback(module: String, version: Int) {

    }

    override fun onUendFrequencyPointCallback(module: String, direction: String, channel: Int) {

    }

    override fun onBluetoothDeviceDiscoverCallback(name: String, address: String) {

    }

    fun refreshBtnLayoutVisible() {
        viewBinding?.wifiBtnLayout?.isVisible = SPConstant.getNetworkManager(context)
    }

}