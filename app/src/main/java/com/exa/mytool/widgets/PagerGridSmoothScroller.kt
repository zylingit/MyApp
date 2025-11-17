package com.aam.mida.mida_yk.widgets

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max


/**
 * 作用：用于处理平滑滚动
 * 摘要：用于用户手指抬起后页面对齐或者 Fling 事件。
 */
class PagerGridSmoothScroller(private val mRecyclerView: RecyclerView) :
    LinearSmoothScroller(mRecyclerView.context) {

    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val manager = mRecyclerView.layoutManager ?: return
        if (manager is PagerGridLayoutManager) {
            val pos = mRecyclerView.getChildAdapterPosition(targetView)
            val snapDistances = manager.getSnapOffset(pos)
            val dx = snapDistances[0]
            val dy = snapDistances[1]
            val time = calculateTimeForScrolling(max(abs(dx), abs(dy)))
            if (time > 0) {
                action.update(dx, dy, time, mDecelerateInterpolator)
            }
        }
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return PagerConfig.millisecondsPreInch / displayMetrics.densityDpi
    }
}