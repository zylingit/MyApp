package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.aam.mida.mida_yk.R;
import com.aam.mida.mida_yk.YKApplication;

import java.util.ArrayList;

public class RadarView extends View {

    //雷达填充颜色
    private int FILL_COLOR = 0x7f00574B;
    //雷达描点
    private int POINT_COLOR = 0xff008577;


    //控件大小
    private int width;
    private int height;
    //控件雷达最大边长
    private int maxRadius;
    private int margin = 100; //px，dp需要自己转换
    //N边形
    private int borderCount = 5;
    //环线个数
    private int circleCount = 1;
    //an angle, in radians,与borderCount相关
    private double angle;

    private int titleTxSize = 12; //px，sp需要自己转换
    private int pointSize = 8; //px，dp需要自己转换
    //标签值
    private String[] mTitles = {YKApplication.app.getString(R.string.ktv_timbre), YKApplication.app.getString(R.string.ktv_skill), YKApplication.app.getString(R.string.ktv_emotion), YKApplication.app.getString(R.string.ktv_rhythm), YKApplication.app.getString(R.string.ktv_intonation),};
    //数据值，个数应该要和borderCount保持一致，不然要做兼容性处理
    private ArrayList<Integer> mData = new ArrayList<>();
    //数据最大值
    private int maxValue = 100;

    private LinearGradient shader;

    //画笔
    private Paint paint;
    private Path path;

    private Bitmap bitmap;


    public RadarView(Context context) {
        super(context, null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.chart_bg);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(255,255, 237,176));
        paint.setAntiAlias(true);
        path = new Path();

//        shader = new LinearGradient(0, 0, 0, 0, Color.parseColor("#FFEDB0FF"), Color.parseColor("#C010CFFF"), Shader.TileMode.CLAMP);
        shader = new LinearGradient(0, 0, 0, 50,
                new int[]{
                        //Color.argb(100,255, 255,255),
                        Color.argb(255,255, 237,176),
                        Color.argb(255,192, 16,207),},
                null, Shader.TileMode.CLAMP);
        //paint.setShader(shader);
        angle = 2 * Math.PI / borderCount;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    public void setData(ArrayList<Integer> data){
        mData.clear();
        mData.addAll(data);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        maxRadius = (Math.min(width, height) - margin) / 2;
        //坐标系移动到控件中心
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.translate(width / 2 + 3 , height / 2 + 16);
        //绘制雷达
        // paint.setColor(Color.parseColor("#FFEDB0FF"));
  /*      canvas.rotate(-15f);
        for (int j = 0; j < borderCount; j++) {
            canvas.rotate(360 / borderCount);
            canvas.drawLine(0, 0, maxRadius, 0, paint);
            for (int i = 1; i <= circleCount; i++) {
                int circleRadius = (int) (i * 1f / circleCount * maxRadius);
                float x = (float) (circleRadius * Math.cos(angle));
                float y = (float) (circleRadius * Math.sin(angle));
                canvas.drawLine(circleRadius, 0, x, y, paint);
            }
        }*/
        //绘制文字标签
      /*  canvas.rotate(15f);
        paint.setTextSize(titleTxSize);
        for (int i = 0; i < borderCount; i++) {
            //int length = maxRadius + 20;
            int x = 0;
            int y = 0;
            switch (i){
                case 0:
                    x =  (int) (maxRadius * Math.cos(i * angle)) + 10 + 20;
                    y = (int) (maxRadius * Math.sin(i * angle)) - 32;
                    canvas.drawCircle(x - 20, y, pointSize, paint);
                    break;
                case 1:
                    x =  (int) (maxRadius * Math.cos(i * angle)) + 32 + 6;
                    y = (int) (maxRadius * Math.sin(i * angle)) + 10 + 16;
                    canvas.drawCircle(x - 4, y - 20, pointSize, paint);
                    break;
                case 2:
                    x =  (int) (maxRadius * Math.cos(i * angle)) - 20 - 8;
                    y = (int) (maxRadius * Math.sin(i * angle)) + 42 + 10;
                    canvas.drawCircle(x + 32, y - 16, pointSize, paint);
                    break;
                case 3:
                    x =  (int) (maxRadius * Math.cos(i * angle)) - 22 - 40;
                    y = (int) (maxRadius * Math.sin(i * angle)) + 22 ;
                    canvas.drawCircle(x + 38, y-2, pointSize, paint);
                    break;
                case 4:
                    x =  (int) (maxRadius * Math.cos(i * angle)) - 42;
                    y = (int) (maxRadius * Math.sin(i * angle)) -20 - 18;
                    canvas.drawCircle(x + 14, y + 18, pointSize, paint);
                    break;
                default:
                    x = 0;
                    y = 0;
                    break;
            }

            canvas.drawText(mTitles[i], x, y, paint);
        }*/

        //绘制多边形并描点填充
        canvas.rotate(-18.5f);
        paint.setShader(shader);
        path.reset();
        for (int i = 0; i < mData.size(); i++) {
            float length = mData.get(i) * 1f / maxValue * maxRadius;
            int x = (int) (length * Math.cos(i * angle));
            int y = (int) (length * Math.sin(i * angle));
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            //canvas.drawCircle(x, y, pointSize, paint);
        }
        canvas.drawPath(path, paint);

    }
}

