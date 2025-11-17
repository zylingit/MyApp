package com.aam.mida.mida_yk.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.IOException;

/**
 * Created by hy on 2017/12/14.
 * 播放手机系统铃声工具类（MediaPlayer播放）
 */

public class RingtoneUtil {

    private static MediaPlayer mMediaPlayer;

    //开始播放铃声
    public static void startPlay(Context context,int mVoiceResId) {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.reset();
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(mVoiceResId);
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //停止播放铃声
    public static void stopPlay() {
        try{
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
