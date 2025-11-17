package com.aam.mida.mida_yk.utils

import android.net.Uri
import com.aam.loglibs.LogUtils
import com.facebook.drawee.backends.pipeline.Fresco

/**
 *  Fresco 缓存管理类
 */
object FrescoCacheManager {
    private const val TAG = "FrescoCacheManager"

    private val picCacheUris = mutableSetOf<String>()

    fun tryAddToCacheUris(url: String?) {
        url ?: return
        LogUtils.i(TAG, "tryAddToCacheUris: $url")
        if (picCacheUris.contains(url).not()) {
            picCacheUris.add(url)
        }
    }

    fun tryClearMemoryCache() {
        LogUtils.i(TAG, "tryClearMemoryCache: ${picCacheUris.size}")
        if (picCacheUris.isNotEmpty()) {
            evictFromCache()
            picCacheUris.clear()
        }
    }

    private fun evictFromCache() {
        if (picCacheUris.isNotEmpty()) {
            picCacheUris.forEach {
                try {
                    val imgUrl = Uri.parse(it)
                    val imagePipeline = Fresco.getImagePipeline()
                    imagePipeline.evictFromCache(imgUrl)
                    LogUtils.i(TAG, "evictFromCache: $imgUrl")
                } catch (e: Exception) {
                    LogUtils.e(TAG, "evictFromCache: ", e)
                }
            }
        }
    }
}