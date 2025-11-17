package com.exa.mytool.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;


public final class InflateUtils {

    private InflateUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 模拟耗时加载
     */
    public static View mockLongTimeLoad(@NonNull ViewGroup parent, int layoutId) {
        try {
            // 模拟耗时
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getInflateView(parent, layoutId);
    }

    public static View getInflateView(@NonNull ViewGroup parent, int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }
}
