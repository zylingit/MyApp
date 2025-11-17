package com.exa.mytool.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.format(formatStr: String, locale: Locale = Locale.getDefault(Locale.Category.FORMAT)): String {
    val sdf = SimpleDateFormat(formatStr, locale)
    return sdf.format(this)
}