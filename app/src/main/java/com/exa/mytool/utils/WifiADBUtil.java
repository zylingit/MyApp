package com.exa.mytool.utils;

import android.annotation.SuppressLint;
import android.os.Build;

import com.aam.loglibs.LogUtils;
import com.aam.mida.base.GlobalVariable;
import com.aam.soundsetting.ProductType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WifiADBUtil {
    private static String propName = "persist.internet_adb_enable";

    private static String hs28PPropName = "persist.vendor.internet.adb.enable";

    public static void enableWifiADB(boolean enable) {
        try {
            Class<?> class1 = null;
            class1 = Class.forName("android.os.SystemProperties");
            String prop = propName;
            if(ProductType.F0P_HS28_2214.equals(GlobalVariable.INSTANCE.getProduceType()) || ProductType.F0K_HS28K_22112.equals(GlobalVariable.INSTANCE.getProduceType())){
                prop = hs28PPropName;
            }
            Method method = class1.getMethod("set", String.class, String.class);
            if (enable) {
                method.invoke(null,prop, "1");
            } else {
                method.invoke(null,prop, "0");
            }
            LogUtils.e("WifiADBUtil", "set WifiADB enable: " + enable);
        } catch (Exception e) {
            LogUtils.e("WifiADBUtil", "enableWifiADB fail", e);
        }
    }

    public static boolean isWifiADBEnable() {
        boolean result = false;
        try {
            Class<?> class1 = null;
            class1 = Class.forName("android.os.SystemProperties");
            String prop = propName;
            if(ProductType.F0P_HS28_2214.equals(GlobalVariable.INSTANCE.getProduceType()) || ProductType.F0K_HS28K_22112.equals(GlobalVariable.INSTANCE.getProduceType())){
                prop = hs28PPropName;
            }
            Method method = class1.getMethod("get", String.class, String.class);
            Object tmpResult= method.invoke(null,prop, "0");
            if (tmpResult.toString().equals("1")) {
                result = true;
            }
        } catch (Exception e) {
            LogUtils.e("WifiADBUtil", "enableWifiADB fail", e);
        }

        return result;
    }

    public static void hookWebView(){
        int sdkInt = Build.VERSION.SDK_INT;
        try {
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
            Field field = factoryClass.getDeclaredField("sProviderInstance");
            field.setAccessible(true);
            Object sProviderInstance = field.get(null);
            if (sProviderInstance != null) {
                //Log.i(TAG,"sProviderInstance isn't null");
                return;
            }

            Method getProviderClassMethod;
            if (sdkInt > 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
            } else if (sdkInt == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
            } else {
               // Log.i(TAG,"Don't need to Hook WebView");
                return;
            }
            getProviderClassMethod.setAccessible(true);
            Class<?> factoryProviderClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
            Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
            Constructor<?> delegateConstructor = delegateClass.getDeclaredConstructor();
            delegateConstructor.setAccessible(true);
            if(sdkInt < 26){//低于Android O版本
                Constructor<?> providerConstructor = factoryProviderClass.getConstructor(delegateClass);
                if (providerConstructor != null) {
                    providerConstructor.setAccessible(true);
                    sProviderInstance = providerConstructor.newInstance(delegateConstructor.newInstance());
                }
            } else {
                @SuppressLint("SoonBlockedPrivateApi") Field chromiumMethodName = factoryClass.getDeclaredField("CHROMIUM_WEBVIEW_FACTORY_METHOD");
                chromiumMethodName.setAccessible(true);
                String chromiumMethodNameStr = (String)chromiumMethodName.get(null);
                if (chromiumMethodNameStr == null) {
                    chromiumMethodNameStr = "create";
                }
                Method staticFactory = factoryProviderClass.getMethod(chromiumMethodNameStr, delegateClass);
                if (staticFactory!=null){
                    sProviderInstance = staticFactory.invoke(null, delegateConstructor.newInstance());
                }
            }

            if (sProviderInstance != null){
                field.set("sProviderInstance", sProviderInstance);
               // Log.i(TAG,"Hook success!");
            } else {
               // Log.i(TAG,"Hook failed!");
            }
        } catch (Throwable e) {
          //  Log.w(TAG,e);
        }
    }


    public void writeToFile(File file, String data) {
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
