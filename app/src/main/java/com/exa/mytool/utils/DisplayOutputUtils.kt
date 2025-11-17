package com.aam.mida.mida_yk.utils

import android.content.Context
import android.os.RkDisplayOutputManager
import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.mida.base.SPConstant
import com.aam.mida.base.entity.ConnectorInfo
import com.aam.mida.base.utils.ReflectUtils
import com.aam.mida.mida_yk.YKApplication
import com.aam.soundsetting.ProductType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author          pgl
 * @time            2023/1/16 13:46
 * @des             屏幕亮度设置
 *
 * @version         1.0v$
 * @updateAuthor    $
 * @updateDate      $
 * @updateDes       屏幕亮度设置
 */
object DisplayOutputUtils {
    private const val TAG = "DisplayOutputUtils"

    private var mScreenBrightness: Int = 50
    private var mScreenContrast: Int = 50
    private var mScreenSaturation: Int = 50
    private var mScreenHue: Int = 50

    private var displayIdList = ArrayList<Int>()

    private val mRkDisplayManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED){
        try {
            Class.forName("android.os.RkDisplayOutputManager").newInstance()
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }


    private val mRkDisplayOutputManager by lazy {
        RkDisplayOutputManager()
    }

    suspend fun init(displayId: Int) = withContext(Dispatchers.IO) {
        getDisplayId()
        getAndSetScreenValue(displayId)
    }

    /**
     * 获取SP保存的亮度值，并设置为当前屏幕亮度
     * */
    private fun getAndSetScreenValue(displayId: Int) {
        val brightness = getScreenBrightness(YKApplication.app)
        val contrast = getScreenContrast(YKApplication.app)
        val saturation = getScreenSaturation(YKApplication.app)
        val hue = getScreenHue(YKApplication.app)

        LogUtils.d(TAG, "getAndSetScreenValue")
        if (GlobalVariable.produceType == ProductType.YYDS_T982_2411) {
            setScreentBrightnessWithT982(
                YKApplication.app,
                displayId,
                brightness
            )
        } else {
            setScreentBrightness(
                YKApplication.app,
                displayId,
                brightness
            )
        }
        setScreentContrast(
            YKApplication.app,
            displayId,
            contrast
        )
        setScreentSaturation(
            YKApplication.app,
            displayId,
            saturation
        )
        setScreentHue(
            YKApplication.app,
            displayId,
            hue
        )
    }


    /**
     * 获取屏幕亮度
     * brightness: [0, 100], default 50
     * */
    fun getScreenBrightness(context: Context): Int {
        mScreenBrightness = SPConstant.getScreenBrightness(context)
        return mScreenBrightness
    }

    /**
     * 获取屏幕对比度
     * brightness: [0, 100], default 50
     * */
    fun getScreenContrast(context: Context): Int {
        mScreenContrast = SPConstant.getScreenContrast(context)
        return mScreenContrast
    }

    /**
     * 获取屏幕饱和度
     * brightness: [0, 100], default 50
     * */
    fun getScreenSaturation(context: Context): Int {
        mScreenSaturation = SPConstant.getScreenSaturation(context)
        return mScreenSaturation
    }

    /**
     * 获取屏幕色度
     * brightness: [0, 100], default 0
     * */
    fun getScreenHue(context: Context): Int {
        mScreenHue = SPConstant.getScreenHue(context)
        return mScreenHue
    }

    private fun getDisplayId(){
        if (GlobalVariable.produceType == ProductType.F0P_HS28_2214 ||
            GlobalVariable.produceType == ProductType.F0K_HS28K_22112) {
            return
        }

        try {
            mRkDisplayOutputManager.updateDispHeader()
        } catch (e: Throwable) {
            LogUtils.w(TAG, "updateDispHeader error, ignore", e)
            return
        }
        val mConnectorList = ArrayList<ConnectorInfo>()
        val info: Array<String> = try {
            mRkDisplayOutputManager.connectorInfo
        } catch (e: Throwable) {
            LogUtils.w(TAG, "get connectorInfo error, ignore", e)
            emptyArray()
        }

        if (info == null || info.isEmpty()) {
            LogUtils.i(TAG, "getDisplayId info.isEmpty")
            return
        }
        for (i in info.indices) {
            val connectorInfo = ConnectorInfo(info[i], i)
            if (connectorInfo.state == 1) {
                mConnectorList.add(connectorInfo)
            }
        }
        val idList = mConnectorList.map { it.dpy }
        LogUtils.i(TAG, "getDisplayId displayId.$idList")
        displayIdList.clear()
        displayIdList.addAll(idList)
    }


    /**
     * 设置屏幕亮度
     * contrast: [0, 100], default 50;
     * */
    fun setScreentBrightness(context: Context, displayId: Int, brightness: Int): Int {
        val brns = if (brightness > 100) {
            100
        } else if (brightness < 0) {
            0
        } else {
            brightness
        }
        if(mRkDisplayManager == null){
            return 0
        }
        
        displayIdList.forEach {
            ReflectUtils.invokeMethod(
                mRkDisplayManager, "setBrightness", arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                ), arrayOf(it, brns)
            )
        }
        if (mScreenBrightness != brns) {
            mScreenBrightness = brns
            SPConstant.setScreenBrightness(context, brns)
        }
        return mScreenBrightness
    }

    /**
     * 设置屏幕亮度，T982专用
     * contrast: [0, 100], default 50;
     * */
    fun setScreentBrightnessWithT982(context: Context, displayId: Int, brightness: Int): Int {
        val brns = if (brightness > 100) {
            100
        } else if (brightness < 0) {
            0
        } else {
            brightness
        }
        val instance = try {
            val instanceMethod = Class.forName("com.droidlogic.app.SystemControlManager").getMethod("getInstance")
            instanceMethod.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
//        SystemControlManager.getInstance().SetBacklight()
        if (instance != null) {
            ReflectUtils.invokeMethod(
                instance, "SetBacklight", arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                ), arrayOf(brns,1)
            )
            if (mScreenBrightness != brns) {
                mScreenBrightness = brns
                SPConstant.setScreenBrightness(context, brns)
            }
        }

        return mScreenBrightness
    }

    /**
     * 设置屏幕对比度
     * contrast: [0, 100], default 50;
     * */
    fun setScreentContrast(context: Context, displayId: Int, value: Int): Int {
        val realValue = if (value > 100) {
            100
        } else if (value < 0) {
            0
        } else {
            value
        }
        if(mRkDisplayManager == null){
            return 0
        }
        displayIdList.forEach {
            ReflectUtils.invokeMethod(
                mRkDisplayManager, "setContrast", arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                ), arrayOf(it, realValue)
            )
        }
        if (mScreenContrast != realValue) {
            mScreenContrast = realValue
            SPConstant.setScreenContrast(context, realValue)
        }
        return mScreenContrast
    }

    /**
     * 设置屏幕饱和度
     * contrast: [0, 100], default 50;
     * */
    fun setScreentSaturation(context: Context, displayId: Int, value: Int): Int {
        val realValue = if (value > 100) {
            100
        } else if (value < 0) {
            0
        } else {
            value
        }
        if(mRkDisplayManager == null){
            return 0
        }
        displayIdList.forEach {
            ReflectUtils.invokeMethod(
                mRkDisplayManager, "setSaturation", arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                ), arrayOf(it, realValue)
            )
        }
        if (mScreenSaturation != realValue) {
            mScreenSaturation = realValue
            SPConstant.setScreentSaturation(context, realValue)
        }
        return mScreenSaturation
    }

    /**
     * 设置屏幕色度
     * contrast: [0, 100], default 0;
     * */
    fun setScreentHue(context: Context, displayId: Int, value: Int): Int {
        val realValue = if (value > 100) {
            100
        } else if (value < 0) {
            0
        } else {
            value
        }
        if(mRkDisplayManager == null){
            return 0
        }
        displayIdList.forEach {
            ReflectUtils.invokeMethod(
                mRkDisplayManager, "setHue", arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                ), arrayOf(it, realValue)
            )
        }
        if (mScreenHue != realValue) {
            mScreenHue = realValue
            SPConstant.setScreenHue(context, realValue)
        }
        return mScreenHue
    }
}