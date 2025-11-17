package com.exa.mytool.utils

import com.aam.beike_nas_mgr.net.ApiFactory
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


fun getStringFromFile(path: String): String {
    var result: String = ""
    if (File(path).exists().not()) {
        return result
    }

    try {
        val f = FileInputStream(path)
        var inputStreamReader: InputStreamReader? = null
        inputStreamReader = InputStreamReader(f, StandardCharsets.UTF_8)
        val bis = BufferedReader(inputStreamReader)
        var line: String? = ""
        while (bis.readLine().also { line = it } != null) {
            result += line
        }
        f.close()
        bis.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

fun getFileSize(url: String): Long {
    var size: Long = 0
    val request = Request.Builder()
        .url(url)
        .head()
        .build()
    try {
        val response: Response = ApiFactory.mClient.newCall(request).execute()
        if (response.isSuccessful) {
            val header: Long? = response.body?.contentLength()
            if (header != null) {
                size = header
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return size
}

