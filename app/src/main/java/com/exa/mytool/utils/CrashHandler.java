package com.aam.mida.mida_yk.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.aam.base.utils.FileManager;
import com.aam.loglibs.LogUtils;
import com.aam.mida.base.GlobalVariable;
import com.aam.mida.base.utils.ShellUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 全局异常监听
 *
 * @author lzy
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    //系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;

    //CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler();

    //程序的Context对象
    private Context mContext;

    /**
     * 用来存储设备信息和异常信息
     */
    private Map<String, String> infos = new HashMap<String, String>();

    /**
     * 用于格式化日期
     */
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * 最大崩溃记录数量，超过数量时，会删除最旧的文件。文件数最大为100，包含精简crash日志以及全量crash日志
     */
    private static int MAX_CRASH_RECORD = 20;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        ex.printStackTrace();
        LogUtils.e(TAG, "crash", new Exception("mid: " + GlobalVariable.INSTANCE.getMid(), ex));

//        LogcatHelper.Companion.getInstance(mContext).stop();
//
//        //使用Toast来显示异常信息
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    if (Looper.myLooper() == null) {
//                        Looper.prepare();
//                    }
//                    Utils.showToast("设备重启中");
//                    Looper.loop();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();

        // 精简版crash begin
        saveCrashInfo2File(mContext, ex);
        // 精简版crash end

        // 全量crash begin
        // 保存全量日志
        saveCrashContextLog();
        // 全量crash end

//        restart();
//        A2BWrapper.INSTANCE.disconnect((aBoolean, o) -> null);

        // 重启APP
        restartApp();
        return true;
    }

    /**
     * 重启APP
     */
    private void restartApp() {
        LogUtils.d(TAG, "--------- restartApp now.--------");
//        System.exit(0);
        ShellUtils.execCommand("am force-stop " + mContext.getPackageName(), true);
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("mid", String.valueOf(GlobalVariable.INSTANCE.getMid()));
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            LogUtils.w(TAG, "an error occured when collect package info;" + e);
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Context context, Throwable ex) {
        //收集设备参数信息
        collectDeviceInfo(context);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + ".log";
            String path = FileManager.INSTANCE.CRASH_PATH() + fileName;
            File dir = new File(FileManager.INSTANCE.CRASH_PATH());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(sb.toString().getBytes());
            fos.close();

            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile()) {
                        String name = pathname.getName();
                        return name.startsWith("crash-");
                    }
                    return false;
                }
            });
            if (files.length > MAX_CRASH_RECORD) {
                // 删除时间最久远的日志文件
                int needDelCount = files.length - MAX_CRASH_RECORD;
                ArrayList<File> fileList = new ArrayList<>();
                for (File file : files) {
                    fileList.add(file);
                }

                // 按时间先后排序
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.lastModified() - o2.lastModified() > 0) {
                            return 1;
                        } else if (o1.lastModified() - o2.lastModified() < 0) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });

                for (int index = 0; index < needDelCount; index++) {
                    fileList.get(index).delete();
                }
            }
            return fileName;
        } catch (Exception e) {
            LogUtils.w(TAG, "an error occur while writing file..." + e);
        }
        return null;
    }

    /**
     * 保存崩溃时的全量日志
     */
    public static void saveCrashContextLog() {
        String logcatFolder = FileManager.INSTANCE.CRASH_PATH() + File.separator;

        File file = new File(logcatFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        long startTime = System.currentTimeMillis();
        Process logcatProc = null;
        BufferedReader mReader = null;
        File logFile = new File(logcatFolder,
                "crash_context_" + formatter.format(new Date()) + ".log");
        LogUtils.d(TAG, "crash context log file: " + logFile.getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(logFile)) {

            LogUtils.d(TAG, "collect crash log begin");
            String cmds = "logcat -b main -d -v threadtime -t 1000";
            logcatProc = Runtime.getRuntime().exec(cmds);
            mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()),
                    1024);
            String line = null;
            do {
                line = mReader.readLine();
                if (line == null) {
                    break;
                }

                if (line.isEmpty()) {
                    continue;
                }

                if (out != null) {
                    out.write((line + "\n").getBytes());
                }
            } while (true);

            out.write("---------------collect crash log end---------------".getBytes());
        } catch (Exception e) {
            Log.e(TAG, "collect crash log error", e);
        } finally {
            if (logcatProc != null) {
                try {
                    logcatProc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mReader != null) {
                try {
                    mReader.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    String name = pathname.getName();
                    return name.startsWith("crash_context_");
                }
                return false;
            }
        });
        if (files.length > MAX_CRASH_RECORD) {
            // 删除时间最久远的日志文件
            int needDelCount = files.length - MAX_CRASH_RECORD;
            ArrayList<File> fileList = new ArrayList<>();
            for (File item : files) {
                fileList.add(item);
            }

            // 按时间先后排序
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.lastModified() - o2.lastModified() > 0) {
                        return 1;
                    } else if (o1.lastModified() - o2.lastModified() < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            for (int index = 0; index < needDelCount; index++) {
                fileList.get(index).delete();
            }
        }
    }

    /**
     * 保存崩溃时的全量日志
     */
    public static String saveCrashContextLog2(Context context) {
        String logcatFolder = new File(context.getFilesDir(), "may_crash_log").getAbsolutePath();

        File file = new File(logcatFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        long startTime = System.currentTimeMillis();
        Process logcatProc = null;
        BufferedReader mReader = null;
        File logFile = new File(logcatFolder,
                "crash_context_" + formatter.format(new Date()) + ".log");
        LogUtils.d(TAG, "may crash context log file: " + logFile.getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(logFile)) {

            LogUtils.d(TAG, "collect crash log begin");
            String cmds = "logcat -b main -b system -b radio -b events -t 100000";
            logcatProc = Runtime.getRuntime().exec(cmds);
            mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()),
                    1024);
            String line = null;
            do {
                line = mReader.readLine();
                if (line == null) {
                    break;
                }

                if (line.isEmpty()) {
                    continue;
                }

                if (out != null) {
                    out.write((line + "\n").getBytes());
                }
            } while (true);

            out.write("---------------collect crash log end---------------\n".getBytes());
            LogUtils.d(TAG, "collect crash log end");
        } catch (Exception e) {
            Log.e(TAG, "collect crash log error", e);
        } finally {
            if (logcatProc != null) {
                try {
                    logcatProc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mReader != null) {
                try {
                    mReader.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    String name = pathname.getName();
                    return name.startsWith("crash_context_");
                }
                return false;
            }
        });
        if (files.length > MAX_CRASH_RECORD) {
            // 删除时间最久远的日志文件
            int needDelCount = files.length - MAX_CRASH_RECORD;
            ArrayList<File> fileList = new ArrayList<>();
            for (File item : files) {
                fileList.add(item);
            }

            // 按时间先后排序
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.lastModified() - o2.lastModified() > 0) {
                        return 1;
                    } else if (o1.lastModified() - o2.lastModified() < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            for (int index = 0; index < needDelCount; index++) {
                fileList.get(index).delete();
            }
        }

        return logFile.getName();
    }
}