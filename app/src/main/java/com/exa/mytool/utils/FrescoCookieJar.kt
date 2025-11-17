package com.exa.mytool.utils

import android.net.Uri
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class FrescoCookieJar: CookieJar {

    private val cookieStoreMap = ConcurrentHashMap<String, ConcurrentHashMap<String, Cookie>>()


    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        var cookieMap = cookieStoreMap[host]
        for (cookie in cookies) {
            if (cookieMap == null) {
                cookieMap = ConcurrentHashMap()
            }
            val cookieName = cookie.name
            cookieMap[cookieName] = cookie
        }
        cookieMap?.let {
            cookieStoreMap[host] = cookieMap
        }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieList: MutableList<Cookie> = ArrayList()
        val cookieMap = cookieStoreMap[url.host]
        if (cookieMap != null) {
            val values: Collection<Cookie> = cookieMap.values
            for (value in values) {
                cookieList.add(value)
            }
        }
        return cookieList
    }

    @Synchronized
    fun loadForHost(host: String?): Map<String, String> {
        val result: MutableMap<String, String> = HashMap()
        val uri = Uri.parse(host)
        val cookieMap = cookieStoreMap[uri.host] ?: return result
        val entries: Set<Map.Entry<String, Cookie>> = cookieMap.entries
        entries.forEach {
            result[it.key] = it.value.toString()
        }
        return result
    }

    fun saveCookie(host: String, cookies: List<Cookie>) {
        var cookieMap = cookieStoreMap[host]
        for (cookie in cookies) {
            if (cookieMap == null) {
                cookieMap = ConcurrentHashMap()
            }
            val cookieName = cookie.name
            cookieMap[cookieName] = cookie
        }
        cookieMap?.let {
            cookieStoreMap[host] = cookieMap
        }
    }


    @Synchronized
    fun clearCookie() {
        cookieStoreMap.clear()
    }
}