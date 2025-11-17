package com.exa.mytool.utils

import android.os.Trace
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.aam.mida.mida_yk.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 预加载辅助工具类
 * 灵感源于：https://blog.csdn.net/alienttech/article/details/106759615
 */
class PreInflateHelper(private val maxCount: Int = 60) {
    private val DEBUG = BuildConfig.DEBUG
    private val mViewCache = ViewCache()
    private var mLayoutInflater: ILayoutInflater = DefaultLayoutInflater.get()
    private var needPreloadCount = -1

    private val scope = MainScope()
    private var preLoadJob: Job? = null

    private var isRelease = false

    /**
     * 剩余缓存数量低于多少时，触发补充缓存。如果设置过高，可能会导致频繁补充缓存，如果设置过低，会导致补充缓存数量多，阻塞IO时间长。
     */
    private val preloadThreshold = (maxCount * 0.75).toInt()

    /**
     * 是否处于补充缓存的过程中
     */
    var isInPreload = false
        private set

    @JvmOverloads
    fun preloadOnce(parent: ViewGroup, layoutId: Int) {
//        preload(parent, layoutId, 1)

        if (isInPreload) {
            return
        }

        val viewsAvailableCount = mViewCache.getViewPoolAvailableCount(layoutId)
        if (viewsAvailableCount > preloadThreshold) {
            return
        }

        Log.d(TAG, "viewsAvailableCount: $viewsAvailableCount, preloadThreshold: $preloadThreshold")
        val preLoadCount = maxCount - viewsAvailableCount
        Log.d(TAG, "batch preload: ${preLoadCount}")
        preLoadInner(parent, layoutId, preLoadCount)
    }

    @JvmOverloads
    fun preload(parent: ViewGroup, layoutId: Int, forcePreCount: Int = 0) {
        val viewsAvailableCount = mViewCache.getViewPoolAvailableCount(layoutId)
        if (viewsAvailableCount >= maxCount) {
            Log.d(TAG, "" + viewsAvailableCount + " > " + maxCount + ", ignore")
            return
        }
        needPreloadCount = maxCount - viewsAvailableCount
        if (forcePreCount > 0) {
            needPreloadCount = Math.min(forcePreCount, needPreloadCount)
        }
        if (DEBUG) {
            Log.d(
                TAG,
                "needPreloadCount:$needPreloadCount, viewsAvailableCount:$viewsAvailableCount"
            )
        }

        if (needPreloadCount <= DEFAULT_PRELOAD_COUNT) {
            for (i in 0 until needPreloadCount) {
                // 异步加载View
                preAsyncInflateView(parent, layoutId)
            }
        } else {
            preLoadInner(parent, layoutId, needPreloadCount)
        }

    }

    private fun preAsyncInflateView(parent: ViewGroup, layoutId: Int) {
        mLayoutInflater.asyncInflateView(parent, layoutId, object : InflateCallback {
            override fun onInflateFinished(layoutId: Int, view: View?) {
                view?: return
                mViewCache.putView(layoutId, view)
                if (DEBUG) {
                    Log.d(
                        TAG,
                        "mViewCache + 1, viewsAvailableCount:" + mViewCache.getViewPoolAvailableCount(
                            layoutId
                        )
                    )
                }
            }
        })
    }

    private suspend fun preAsyncInflateView2(parent: ViewGroup, layoutId: Int) = suspendCancellableCoroutine<Unit> {
        mLayoutInflater.asyncInflateView(parent, layoutId, object : InflateCallback {
            override fun onInflateFinished(layoutId: Int, view: View?) {
                if (isRelease) {
                    mViewCache.mViewPools.clear()
                    return
                }

                if (view == null) {
                    return
                }
                mViewCache.putView(layoutId, view)
                if (DEBUG) {
                    Log.d(
                        TAG,
                        "mViewCache + 1, viewsAvailableCount:" + mViewCache.getViewPoolAvailableCount(
                            layoutId
                        )
                    )
                }

                it.resumeWith(Result.success(Unit))
            }
        })
    }

    private suspend fun preAsyncInflateView3(parent: ViewGroup, layoutId: Int, batchCount: Int) = suspendCancellableCoroutine<Unit> {
        if (DEBUG) {
            Log.d(TAG, "batch inflate begin, count: ${batchCount}")
        }
        mLayoutInflater.asyncBatchInflateView(parent, layoutId, batchCount, object : BatchInflateCallback {
            override fun onInflateFinished(layoutId: Int, view: List<View>?) {
                if (DEBUG) {
                    Log.d(TAG, "batch inflate end, count: ${view?.size}")
                }

                view?.forEach {
                    mViewCache.putView(layoutId, it)
                }

                if (isRelease) {
                    if (DEBUG) {
                        Log.d(TAG, "clear mViewPools")
                    }
                    mViewCache.mViewPools.clear()
                    if (it.isCompleted.not()) {
                        it.resumeWith(Result.success(Unit))
                    }
                    return
                }

                if (DEBUG) {
                    Log.d(TAG, "batch add view cache, viewsAvailableCount:"
                        + mViewCache.getViewPoolAvailableCount(layoutId))
                }

                if (it.isCompleted.not()) {
                    it.resumeWith(Result.success(Unit))
                }
            }
        })
    }

    fun getView(parent: ViewGroup, layoutId: Int): View {
        return getView(parent, layoutId, DEFAULT_PRELOAD_COUNT)
    }

    fun getView(parent: ViewGroup, layoutId: Int, maxCount: Int): View {
        val view = mViewCache.getView(layoutId)
        if (view != null) {
            if (DEBUG) {
                Log.d(TAG, "get view from cache!")
            }

//            if (maxCount > 20) {
//                if (mViewCache.getViewPoolAvailableCount(layoutId) < 20) {
//                    preLoadInner(parent, layoutId, maxCount)
//                }
//            } else {
//                preloadOnce(parent, layoutId)
//            }
            preloadOnce(parent, layoutId)
            return view
        }

        if (DEBUG) {
            Log.d(TAG, "inflateView!")
        }
        preloadOnce(parent, layoutId)
        return mLayoutInflater.inflateView(parent, layoutId)
    }

    fun setAsyncInflater(asyncInflater: ILayoutInflater): PreInflateHelper {
        mLayoutInflater = asyncInflater
        return this
    }

    private fun preLoadInner(parent: ViewGroup, layoutId: Int, preLoadCount: Int) {
        if (preLoadJob != null) {
            return
        }

        isInPreload = true
        preLoadJob = scope.launch {
            Trace.beginSection("batch preload")
            Log.d(TAG, "pre load start")
//            (0 until preLoadCount).map {
//                async {
//                    // 异步加载View
//                    preAsyncInflateView2(parent, layoutId)
//                }
//            }.awaitAll()

            preAsyncInflateView3(parent, layoutId, preLoadCount)

            do {
                val viewsAvailableCount = mViewCache.getViewPoolAvailableCount(layoutId)
                if (viewsAvailableCount <= preloadThreshold) {
                    val preLoadCount2 = maxCount - viewsAvailableCount
                    Log.d(TAG, " preload again, viewsAvailableCount: $viewsAvailableCount, preLoadCount: $preLoadCount2")
                    preAsyncInflateView3(parent, layoutId, preLoadCount2)
                }
            } while (mViewCache.getViewPoolAvailableCount(layoutId) <= preloadThreshold && isActive)

            isInPreload = false
            Log.d(TAG, "pre load end")
            Trace.endSection()
        }

        preLoadJob?.invokeOnCompletion {
            Trace.endSection()
            isInPreload = false
            preLoadJob = null
        }
    }

    fun clearCache() {
        mViewCache.mViewPools.clear()
    }

    fun destroy() {
        Log.d(TAG, "destroy")
        mLayoutInflater.release()
        scope.cancel()
        isRelease = true
    }

    ///////////////////////////////////////////////////////////////////////////
    // 内部类
    ///////////////////////////////////////////////////////////////////////////
    private class ViewCache {
        val mViewPools = HashMap<Int, ArrayList<View>>()

        fun getViewPool(layoutId: Int): ArrayList<View> {
            var views = mViewPools[layoutId]
            if (views == null) {
                views = ArrayList()
                mViewPools.put(layoutId, views)
            }
            return views
        }

        fun getViewPoolAvailableCount(layoutId: Int): Int {
//            val views = getViewPool(layoutId)
//            val it = views.iterator()
//            var count = 0
//            while (it.hasNext()) {
//                if (it.next() != null) {
//                    count++
//                } else {
//                    it.remove()
//                }
//            }
//            return count
            return getViewPool(layoutId).size
        }

        fun putView(layoutId: Int, view: View) {
            if (view == null) {
                return
            }
            getViewPool(layoutId).add(view)
        }

        fun putViews(layoutId: Int, views: List<View>) {
            getViewPool(layoutId).addAll(views)
        }

        fun getView(layoutId: Int): View? {
            return getViewFromPool(getViewPool(layoutId))
        }

        private fun getViewFromPool(views: ArrayList<View>): View? {
            return if (views.isEmpty()) {
                null
            } else views.removeFirstOrNull() ?: return getViewFromPool(views)
        }
    }

    interface ILayoutInflater {
        /**
         * 异步加载View
         *
         * @param parent   父布局
         * @param layoutId 布局资源id
         * @param callback 加载回调
         */
        fun asyncInflateView(parent: ViewGroup, layoutId: Int, callback: InflateCallback?)

        /**
         * 批量异步加载View
         *
         * @param parent   父布局
         * @param layoutId 布局资源id
         * @param callback 加载回调
         */
        fun asyncBatchInflateView(parent: ViewGroup, layoutId: Int, batchCount: Int, callback: BatchInflateCallback?)

        /**
         * 同步加载View
         *
         * @param parent   父布局
         * @param layoutId 布局资源id
         * @return 加载的View
         */
        fun inflateView(parent: ViewGroup, layoutId: Int): View

        fun release()
    }

    interface InflateCallback {
        fun onInflateFinished(layoutId: Int, view: View?)
    }

    interface BatchInflateCallback {

        fun onInflateFinished(layoutId: Int, view: List<View>?)
    }

    companion object {
        private const val TAG = "PreInflateHelper"

        /**
         * 默认的预加载缓存池大小，默认是5，可根据需求设置
         */
        const val DEFAULT_PRELOAD_COUNT = 5
    }
}