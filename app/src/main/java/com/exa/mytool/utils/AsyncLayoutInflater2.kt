package com.aam.mida.mida_yk.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.UiThread
import androidx.core.util.Pools
import java.util.concurrent.ArrayBlockingQueue

class AsyncLayoutInflater2(private val context: Context) {
    private val TAG = "AsyncLayoutInflater2"

    private var mInflater: LayoutInflater? = null
    private var mHandler: Handler? = null
    private var mInflateThread: InflateThread = InflateThread()

    private val mHandlerCallback = Handler.Callback { msg ->
        when (msg.what) {
            0 -> {
                val request = msg.obj as InflateRequest
                if (request.view == null) {
                    request.view = mInflater?.inflate(
                        request.resid, request.parent, false
                    )
                }
                request.callback?.onInflateFinished(
                    request.view!!, request.resid, request.parent
                )

                request.callback = null
                mInflateThread.releaseRequest(request)
            }
            1 -> {
                val request = msg.obj as InflateRequest
                val resultList = request.viewList?: emptyList()
                request.batchInflateCallback?.onInflateFinished(
                    resultList, request.resid, request.parent
                )
                request.batchInflateCallback = null
                mInflateThread.releaseRequest(request)
            }
        }

        true
    }

    init {
        mInflater = BasicInflater(context)
        mHandler = Handler(Looper.getMainLooper(), mHandlerCallback)
        mInflateThread.start()
    }

    @UiThread
    fun inflate(@LayoutRes resid: Int, parent: ViewGroup?, callback: OnInflateFinishedListener) {
        if (callback == null) {
            throw NullPointerException("callback argument may not be null!")
        }
        val request = mInflateThread.obtainRequest()
        request.inflater = this
        request.resid = resid
        request.parent = parent
        request.callback = callback
        request.isBatch = false
        mInflateThread.enqueue(request)
    }

    /**
     * 批量创建
     */
    @UiThread
    fun batchInflate(
        @LayoutRes resid: Int, parent: ViewGroup?, inflateCount: Int,
        callback: OnBatchInflateFinishedListener) {
        if (callback == null) {
            throw NullPointerException("callback argument may not be null!")
        }
        val request = mInflateThread.obtainRequest()
        request.inflater = this
        request.resid = resid
        request.parent = parent
        request.batchCount = inflateCount
        request.batchInflateCallback = callback
        request.isBatch = true
        mInflateThread.enqueue(request)
    }

    interface OnInflateFinishedListener {
        fun onInflateFinished(
            view: View, @LayoutRes resid: Int,
            parent: ViewGroup?
        )
    }

    interface OnBatchInflateFinishedListener {
        fun onInflateFinished(
            view: List<View>, @LayoutRes resid: Int, parent: ViewGroup?)
    }

    inner class InflateRequest {
        var inflater: AsyncLayoutInflater2? = null
        var parent: ViewGroup? = null
        var resid = 0
        var view: View? = null
        var viewList: List<View>? = null
        var batchCount: Int = 1
        var callback: OnInflateFinishedListener? = null
        var batchInflateCallback: OnBatchInflateFinishedListener? = null
        var isBatch: Boolean = false
    }

    private class BasicInflater internal constructor(context: Context?) :
        LayoutInflater(context) {
        override fun cloneInContext(newContext: Context): LayoutInflater {
            return BasicInflater(newContext)
        }

        @Throws(ClassNotFoundException::class)
        override fun onCreateView(name: String, attrs: AttributeSet): View {
            for (prefix in sClassPrefixList) {
                try {
                    val view = createView(name, prefix, attrs)
                    if (view != null) {
                        return view
                    }
                } catch (e: ClassNotFoundException) {
                    // In this case we want to let the base class take a crack
                    // at it.
                }
            }
            return super.onCreateView(name, attrs)
        }

        companion object {
            private val sClassPrefixList = arrayOf(
                "android.widget.",
                "android.webkit.",
                "android.app."
            )
        }
    }

    fun release() {
        mInflateThread.release()
        mHandler?.removeCallbacksAndMessages(0)
    }

    inner class InflateThread : Thread() {
        private val mQueue = ArrayBlockingQueue<InflateRequest>(100)
        private val mRequestPool = Pools.SynchronizedPool<InflateRequest>(100)

        private var isDestroy = false

        // Extracted to its own method to ensure locals have a constrained liveness
        // scope by the GC. This is needed to avoid keeping previous request references
        // alive for an indeterminate amount of time, see b/33158143 for details
        fun runInner() {
            val request: InflateRequest = try {
                mQueue.take()
            } catch (ex: InterruptedException) {
                // Odd, just continue
                Log.w(TAG, ex)
                return
            }

            if (isDestroy) {
                return
            }

            if (request.isBatch) {
                val list = mutableListOf<View>()
                for (index in 0 until request.batchCount) {
                    try {
                        request.inflater?.mInflater?.inflate(
                            request.resid, request.parent, false
                        )?.let {
                            list.add(it)
                        }
                    } catch (ex: RuntimeException) {
                        // Probably a Looper failure, retry on the UI thread
                        Log.w(
                            TAG, "Failed to inflate resource in the background! Retrying on the UI"
                                + " thread", ex
                        )
                    }
                }

                request.viewList = list

                request.inflater?.mHandler?.let {
                    Message.obtain(it, 1, request).sendToTarget()
                }
            } else {
                try {
                    request.view = request.inflater?.mInflater?.inflate(
                        request.resid, request.parent, false
                    )
                } catch (ex: RuntimeException) {
                    // Probably a Looper failure, retry on the UI thread
                    Log.w(
                        TAG, "Failed to inflate resource in the background! Retrying on the UI"
                            + " thread", ex
                    )
                }

                request.inflater?.mHandler?.let {
                    Message.obtain(it, 0, request).sendToTarget()
                }
            }

            if (isDestroy) {
                return
            }
        }

        override fun run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY)
            while (isDestroy.not()) {
                runInner()
            }
        }

        fun obtainRequest(): InflateRequest {
            var obj = mRequestPool.acquire()
            if (obj == null) {
                obj = InflateRequest()
            }
            return obj
        }

        fun releaseRequest(obj: InflateRequest) {
            obj.callback = null
            obj.inflater = null
            obj.parent = null
            obj.resid = 0
            obj.view = null
            obj.viewList = null
            mRequestPool.release(obj)
        }

        fun enqueue(request: InflateRequest) {
            try {
                mQueue.put(request)
            } catch (e: InterruptedException) {
                throw RuntimeException(
                    "Failed to enqueue async inflate request", e
                )
            }
        }

        fun release() {
            isDestroy = true
            enqueue(InflateRequest())
        }
    }
}