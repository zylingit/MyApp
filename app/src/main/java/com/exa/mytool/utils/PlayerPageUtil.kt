package com.aam.mida.mida_yk.utils

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aam.mida.base.GlobalBroadcastAction
import com.aam.mida.base.ext.killOtherAppProcess
import com.aam.mida.mida_yk.YKApplication
import com.aam.mida.mida_yk.activity.MovieDetailActivity
import com.aam.mida.mida_yk.entity.ToMoviePlayIntentEntry

/**
 *
 * @Description 用于跳转到KTVPlayActivity 或者 MoviePlayActivity
 * @Author zechao.zhang
 * @CreateTime 2024/05/22
 */

fun toMoviePlayActivity(entry: ToMoviePlayIntentEntry = ToMoviePlayIntentEntry(fromType = MovieDetailActivity.PAGE_TYPE_COMMON)) {
    LocalBroadcastManager.getInstance(YKApplication.app).sendBroadcast(Intent().apply {
        action = GlobalBroadcastAction.ACTION_OPEN_MOVIE_PLAY_ACTIVITY
        putExtra("movieEntry", entry)
    })
}

fun toKtvPlayActivity(){
    LocalBroadcastManager.getInstance(YKApplication.app).sendBroadcast(Intent().apply {
        action = GlobalBroadcastAction.ACTION_OPEN_KTV_PLAY_ACTIVITY
    })
}

fun killForegroundProcessIfNeed() {
    if (YKApplication.app.isAppInBackground) {
//        val mActivityManager = YKApplication.app.getSystemService(
//            AppCompatActivity.ACTIVITY_SERVICE
//        ) as ActivityManager
//
//        for (appProcess in mActivityManager.runningAppProcesses) {
//            if (appProcess.processName != YKApplication.app.packageName) {
//                val result = ShellUtils.execCommand(
//                    "am force-stop ${appProcess.processName}",
//                    true
//                )
//                LogUtils.d("PlayerPageUtil", "killProcess pid ${appProcess.processName} result ${result.result}")
//            } else {
//                break
//            }
//        }
        killOtherAppProcess()
    }
}