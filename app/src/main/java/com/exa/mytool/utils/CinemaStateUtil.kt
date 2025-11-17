package com.exa.mytool.utils

object CinemaStateUtil {

    //放映状态(0：未开始 1：即将开始 2：正在播放 3：即将结束 4：已结束)
    const val STATE_NO_START = 0  //未开始
    const val STATE_WILL_START = 1 //即将开始
    const val STATE_PLAYING = 2  //播放中
    const val STATE_WILL_END = 3  //即将结束
    const val STATE_END = 4  // 已结束



    const val STATE_NO_START_BUY = 0  //未开始 -已购买
    const val STATE_WILL_START_BUY = 2 //即将开始 -已购买
    const val STATE_PLAYING_BUY = 4  //播放中 -已购买
    const val STATE_WILL_END_BUY = 6  //即将结束 -已购买
    const val STATE_ENDED = 8  // 已结束


    private const val MINUTES_10 = 10 * 60 * 1000
    private const val MINUTES_15 = 15 * 60 * 1000
    private const val MINUTES_60 = 60 * 60 * 1000

    fun getCinemaState(startTimeMs: Long, endPlayTime: Long): Int {
        val currentTime = System.currentTimeMillis()
        val diffTimeEnd = endPlayTime - currentTime //距离结束还剩多久
        val diffTimeStart = startTimeMs - currentTime  //距离开始还有多久

        when {
            currentTime in startTimeMs..endPlayTime -> {
                return if  (diffTimeEnd < MINUTES_15) STATE_WILL_END_BUY else STATE_PLAYING_BUY
            }
            currentTime < startTimeMs -> {
//                return if (diffTimeStart <= MINUTES_60) STATE_WILL_START_BUY else STATE_NO_START_BUY
                return if (diffTimeStart <= MINUTES_10) STATE_WILL_START_BUY else STATE_NO_START_BUY
            }
            else -> {
                //已结束
                return STATE_ENDED
            }
        }
    }
}