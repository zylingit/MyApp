package com.aam.mida.mida_yk.widgets

import android.app.Instrumentation
import android.view.KeyEvent
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TitleBarHelper {

    private var backLayout: View? = null
    private val instrumentation = Instrumentation()

    /**
     * 左侧优先显示上一个activity的title
     */
    constructor( backLayouts: View,leftStrId: Int) {
        backLayout = backLayouts
        backLayout?.setOnClickListener {
//            mActivity!!.onBackPressed()
            keyEventBackPressed()
        }
        setLeftMsg(leftStrId)
    }

    /** 右侧 end *********************/

    /** 右侧 end  */
    /** 左侧 begin  */
    fun setLeftMsg(leftStrId: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            if (leftStrId == -1) {
                backLayout?.visibility = View.GONE
            } else {
                backLayout?.visibility = View.VISIBLE
            }
        }
    }

    fun closeGlobalBack(){
        GlobalScope.launch (Dispatchers.Main){
            backLayout?.visibility = View.GONE
        }
    }

    fun openGlobalBack(){
        GlobalScope.launch (Dispatchers.Main){
            backLayout?.visibility = View.VISIBLE
        }
    }


    fun keyEventBackPressed(){
        GlobalScope.launch(Dispatchers.IO) {
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        }
    }

    /** 左侧按钮  */
    fun setOnLeftClickListener(listener: View.OnClickListener?) {
        backLayout?.setOnClickListener(listener)
    }

    /** 左侧 end *********************/


}