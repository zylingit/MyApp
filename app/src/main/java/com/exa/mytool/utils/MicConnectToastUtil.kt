package com.exa.mytool.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.aam.mida.mida_yk.R

object MicConnectToastUtil {
    fun showToast(context: Context, text: String) {
        try {
            val layout =
                LayoutInflater.from(context).inflate(R.layout.dialog_microphone_auto_connect, null)
            layout.findViewById<TextView>(R.id.tvTips).text = text
            val toast = Toast(context.applicationContext)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layout
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}