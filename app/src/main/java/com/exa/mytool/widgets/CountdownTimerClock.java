package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by lzy on 2025/3/26
 * Description：可自定义开始时间的定时器
 */
public class CountdownTimerClock extends AppCompatTextView {
    private static final String TAG = "CountdownTimerClock";
    private long mStartTime = 0; // 初始时间
    private boolean mIsRunning = false;
    private final Handler mHandler = new Handler();
    private Runnable mUpdateRunnable;

    public CountdownTimerClock(Context context) {
        super(context);
        init();
    }

    public CountdownTimerClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (mIsRunning) {
                    updateTimeDisplay();
                    mHandler.postDelayed(this, 1000); // 每秒更新
                }
            }
        };
    }

    // 设置初始时间（毫秒）
    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    // 开始计时
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mHandler.post(mUpdateRunnable);
        }
    }

    // 停止计时
    public void stop() {
        mIsRunning = false;
        mHandler.removeCallbacks(mUpdateRunnable);
    }

    // 更新显示
    private void updateTimeDisplay() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - mStartTime;

        // 转换为时分秒格式
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // 忽略时区偏移
        String formattedTime = sdf.format(new Date(elapsed));
        setText(formattedTime);
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
//        Log.i(TAG, "onVisibilityAggregated:" + isVisible);
        if (isVisible) {
            start();
        } else {
            stop();
        }
    }

}

