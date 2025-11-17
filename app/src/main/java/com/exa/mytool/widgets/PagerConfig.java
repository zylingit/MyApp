package com.aam.mida.mida_yk.widgets;

/**
 * 作用：Pager配置
 */
public class PagerConfig {
    public static final String TAG = "PagerGrid";

    /**
     * 垂直滚动
     */
    public static final int VERTICAL = 0;
    /**
     * 水平滚动
     */
    public static final int HORIZONTAL = 1;

    /**
     * Fling 阀值，滚动速度超过该阀值才会触发滚动
     */
    public static final int flingThreshold = 1000;

    /**
     * 每一个英寸滚动需要的微秒数，数值越大，速度越慢
     */
    public static final Float millisecondsPreInch = 60f;
}