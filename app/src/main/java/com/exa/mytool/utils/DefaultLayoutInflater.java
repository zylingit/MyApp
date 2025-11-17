package com.aam.mida.mida_yk.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;

public class DefaultLayoutInflater implements PreInflateHelper.ILayoutInflater {

    private AsyncLayoutInflater2 mInflater;

    private DefaultLayoutInflater() {

    }

    @Override
    public void asyncBatchInflateView(@NonNull ViewGroup parent, int layoutId, int batchCount,
                                      @Nullable PreInflateHelper.BatchInflateCallback callback) {
        if (mInflater == null) {
            Context context = parent.getContext();
            mInflater = new AsyncLayoutInflater2(new ContextThemeWrapper(context.getApplicationContext(), context.getTheme()));
        }

        mInflater.batchInflate(layoutId, parent, batchCount, (viewList, resId, parent1) -> {
            if (callback != null) {
                callback.onInflateFinished(resId, viewList);
            }
        });
    }

    @Override
    public void release() {
        if (mInflater != null) {
            mInflater.release();
        }
        mInflater = null;
    }

    private static final class InstanceHolder {
        static final DefaultLayoutInflater sInstance = new DefaultLayoutInflater();
    }

    public static DefaultLayoutInflater get() {
        return InstanceHolder.sInstance;
    }

    @Override
    public void asyncInflateView(@NonNull ViewGroup parent, int layoutId, PreInflateHelper.InflateCallback callback) {
        if (mInflater == null) {
            Context context = parent.getContext();
            mInflater = new AsyncLayoutInflater2(new ContextThemeWrapper(context.getApplicationContext(), context.getTheme()));
        }
        mInflater.inflate(layoutId, parent, (view, resId, parent1) -> {
            if (callback != null) {
                callback.onInflateFinished(resId, view);
            }
        });
    }

    @Override
    public View inflateView(@NonNull ViewGroup parent, int layoutId) {
        return InflateUtils.getInflateView(parent, layoutId);
    }
}
