package com.aam.mida.mida_yk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.util.Log;

import com.aam.mida.mida_yk.R;
import com.aam.mida.mida_yk.YKApplication;
import com.qiniu.android.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

/**
 * Created by lzy on 2024/3/28
 * Description：弹幕工具类
 */
public class DanmakuConfigUtil {

    private static final String TAG = "DanmakuConfigUtil";

    /**
     * 初始化
     */
    public static DanmakuContext getDanmakuContext() {
        //设置显示最大行数
        Map<Integer, Integer> maxLines = new HashMap<>();
        maxLines.put(BaseDanmaku.TYPE_SCROLL_RL, 3);

        //设置是否显示重叠
        Map<Integer, Boolean> overMap = new HashMap<>();
        overMap.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overMap.put(BaseDanmaku.TYPE_FIX_TOP, true);

        //实例化弹幕上下文
        DanmakuContext mDmkContext = DanmakuContext.create();
        mDmkContext
                .setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false)  //不可重复合并
                .setScrollSpeedFactor(1.2f)   //设置滚动速度因子
                .setScaleTextSize(1.2f)    //弹幕字体缩放
                .setMaximumLines(maxLines)   //设置最大滚动行
                .preventOverlapping(overMap).setDanmakuMargin(40);
        return mDmkContext;
    }

    /**
     * 生成默认解析
     */
    public static BaseDanmakuParser getDefaultDanmakuParser() {
        return new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        };
    }

    /**
     * 获取一条弹幕
     */
    public static BaseDanmaku addDanmu(Context context, DanmakuContext mDmkContext, long time, String content, Bitmap bitmap) {
        //创建一条从右侧开始滚动的弹幕
        BaseDanmaku danmaku = mDmkContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null) {
            return null;
        }
        danmaku.text = " "+content+" ";
        danmaku.priority = 0;
        danmaku.isLive = true;
        danmaku.setTime(time + 1200);
        danmaku.textSize = context.getResources().getDimension(R.dimen.px24);
        danmaku.textColor = Color.WHITE;
        setDanmuBackgroundColor(mDmkContext, bitmap);
        return danmaku;
    }

    /**
     * 设置背景
     */
    private static void setDanmuBackgroundColor(DanmakuContext mDmkContext, Bitmap bitmap) {
        mDmkContext.setCacheStuffer(new BackgroundCacheStuffer(bitmap), mCacheStufferAdapter);
    }


    /**
     * 通过扩展SpannedCacheStuffer个性化你的弹幕样式
     */
    private static class BackgroundCacheStuffer extends SpannedCacheStuffer {
        Bitmap bitmap;

        public BackgroundCacheStuffer(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
            danmaku.padding = 10;  // 在背景绘制模式下增加padding
            //设置抗锯齿
            paint.setAntiAlias(true);
            //使文本看起来更清晰
            paint.setLinearText(true);
            super.measure(danmaku, paint, fromWorkerThread);
        }

        @Override
        public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {
            //弹幕背景颜色
            Log.i(TAG, "drawBackground: bitmap  " + bitmap);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(YKApplication.app.getResources(), R.drawable.danmu);
            }
            NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(YKApplication.app.getResources(), bitmap, bitmap.getNinePatchChunk(), new Rect(), null);
            Rect rectSrc = new Rect(0, 0, (int) danmaku.paintWidth, (int) danmaku.paintHeight);
            ninePatchDrawable.setBounds(rectSrc);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            ninePatchDrawable.draw(canvas);
        }

        @Override
        public void drawText(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, TextPaint paint, boolean fromWorkerThread) {
            super.drawText(danmaku, lineText, canvas, left, top, paint, fromWorkerThread);
        }

        @Override
        public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint) {
            super.drawStroke(danmaku, lineText, canvas, left, top, paint);
        }
    }


    /**
     * 创建填充适配器
     */
    private static final BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {
        @Override
        public void prepareDrawing(BaseDanmaku danmaku, boolean fromWorkerThread) {
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
        }
    };
}

