package com.exa.mytool.utils

import com.aam.mida.mida_yk.entity.MovieEntry

/**
 *Created by lzy on 2022/12/5
 *Description：一些界面间的回调函数
 */
object GlobalFunctCallback {
    /**
     * 进入私人影院-准备界面回调
     */
    var enterPersonalMovieReadyCallBack: ((String) -> Unit)? = null

    /**
     * 退出私人影院-准备界面回调
     */
    var exitPersonalMovieReadyCallBack: (() -> Unit)? = null

    /**
     * 通知私人影院-准备界面出现关闭弹窗
     */
    var closePersonalMovieReadyCallBack: (() -> Unit)? = null

    /**
     * 私人影院-准备界面出现关闭弹窗
     * Boolean 是否确定关闭
     */
    var personalMovieReadyDialogCallBack: ((Boolean) -> Unit)? = null

    /**
     * 私人影院-监听返回键
     */
    var personalKeyCodeCallBack: ((Int) -> Unit)? = null

    /**
     * 私人影院-监听向下键
     */
    var personalKeyCodeDownCallBack: ((Int) -> Unit)? = null

    /**
     * 私人影院-播放界面，判断是主动退出，还是播放结束后退出
     * Boolean 是否主动退出
     */
    var personalMoviePlayCloseCallBack: ((Boolean) -> Unit)? = null

    /**
     * 私人影院-播放界面，判断某个人在说话，显示帧动画
     */
    var personalMoviePlaySpeakingCallBack: ((Int, Boolean) -> Unit)? = null

    /**
     * Dialog显示时，通知最外层Activity显示背景蒙版
     */
    var movieActivityShowDialogBgCallBack: ((Boolean) -> Unit)? = null


    /**
     * 私人影院创建房间，购买电影后返回，默认填充数据
     */
    var movieDetailPaySuccessCallBack: ((MovieEntry) -> Unit)? = null



    /**
     * 咪哒影院tab 监听向上、向下键
     */
    var movieKeyCodeTopOrDownCallBack: ((Boolean) -> Unit)? = null

    /**
     * 应用中心tab 监听向上、向下键
     */
    var appCenterKeyCodeTopOrDownCallBack: ((Boolean) -> Unit)? = null
}