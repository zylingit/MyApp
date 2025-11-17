package com.aam.mida.mida_yk.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent

/**
 * 反向扫码相关
 */
class ScanKeyManager(var mListener: OnScanValueListener?) {
    private val mResult: StringBuilder = StringBuilder()
    private var mCaps = false

    private val runnable = Runnable {
        mListener?.onScanValue(mResult.toString())
        mResult.delete(0, mResult.length)
    }

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        val TAG = "ScanKeyManager"
    }

    interface OnScanValueListener {
        fun onScanValue(value: String?)
    }

    /**
     * 扫码设备事件解析
     */
    fun analysisKeyEvent(event: KeyEvent) {
        val keyCode = event.keyCode
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (mCaps.not()) {
                checkLetterStatus(event)

                if (mCaps) {
                    return
                }
            }

            val aChar = getInputCode(mCaps, event.keyCode)
            if (aChar.toInt() != 0 && aChar != ' ') {
                mResult.append(aChar)
            }

//            Log.d(TAG, "keyCode: $keyCode")
//            Log.d(TAG, "current str: $mResult")
            mCaps = false
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 100)
            /*if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (mListener != null) {
                    mListener!!.onScanValue(mResult.toString())
                }
                mResult.delete(0, mResult.length)
            }*/
        }
    }

    /**
     * 判断大小写
     */
    private fun checkLetterStatus(event: KeyEvent) {
//        LogUtils.d(TAG, "checkLetterStatus: ${event.toString()}")
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            mCaps = event.action == KeyEvent.ACTION_DOWN
        } else {
            mCaps = false
        }
    }

    /**
     * 将keyCode转为char
     *
     * @param caps    是不是大写
     * @param keyCode 按键
     * @return 按键对应的char
     */
    private fun getInputCode(caps: Boolean, keyCode: Int): Char {
//        LogUtils.d(TAG, "caps: $caps")
        return if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            ((if (caps) 'A' else 'a').toInt() + keyCode - KeyEvent.KEYCODE_A).toChar()
        } else {
            keyValue(caps, keyCode)
        }
    }

    /**
     * 按键对应的char表
     */
    private fun keyValue(caps: Boolean, keyCode: Int): Char {
        return when (keyCode) {
            KeyEvent.KEYCODE_0 -> if (caps) ')' else '0'
            KeyEvent.KEYCODE_1 -> if (caps) '!' else '1'
            KeyEvent.KEYCODE_2 -> if (caps) '@' else '2'
            KeyEvent.KEYCODE_3 -> if (caps) '#' else '3'
            KeyEvent.KEYCODE_4 -> if (caps) '$' else '4'
            KeyEvent.KEYCODE_5 -> if (caps) '%' else '5'
            KeyEvent.KEYCODE_6 -> if (caps) '^' else '6'
            KeyEvent.KEYCODE_7 -> if (caps) '&' else '7'
            KeyEvent.KEYCODE_8 -> if (caps) '*' else '8'
            KeyEvent.KEYCODE_9 -> if (caps) '(' else '9'
            KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> '-'
            KeyEvent.KEYCODE_MINUS -> '_'
            KeyEvent.KEYCODE_EQUALS -> '='
            KeyEvent.KEYCODE_NUMPAD_ADD -> '+'
            KeyEvent.KEYCODE_GRAVE -> if (caps) '~' else '`'
            KeyEvent.KEYCODE_BACKSLASH -> if (caps) '|' else '\\'
            KeyEvent.KEYCODE_LEFT_BRACKET -> if (caps) '{' else '['
            KeyEvent.KEYCODE_RIGHT_BRACKET -> if (caps) '}' else ']'
            KeyEvent.KEYCODE_SEMICOLON -> if (caps) ':' else ';'
            KeyEvent.KEYCODE_APOSTROPHE -> if (caps) '"' else '\''
            KeyEvent.KEYCODE_COMMA -> if (caps) '<' else ','
            KeyEvent.KEYCODE_PERIOD -> if (caps) '>' else '.'
            KeyEvent.KEYCODE_SLASH -> if (caps) '?' else '/'
            else -> ' '
        }
    }
}