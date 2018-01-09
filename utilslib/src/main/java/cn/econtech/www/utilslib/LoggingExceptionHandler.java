package cn.econtech.www.utilslib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by Admin-01 on 2017/12/28.
 * logging uncaught exception to file
 */

public class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final static String TAG = LoggingExceptionHandler.class.getSimpleName();
    private final Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;


    public LoggingExceptionHandler(Context context) {
        mContext = context;
        // we should store the current exception handler -- to invoke it for all not handled exceptions ...
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        try {
            Log.d(TAG, "called for " + ex.toString());
            FileLogger.getInstance().logMsg(collectDeviceInfo(mContext), "exception.txt", true);
            FileLogger.getInstance().logMsg(getExceptionDetails(ex), "exception.txt", true);
            mDefaultHandler.uncaughtException(thread, ex);
        } catch (Exception e) {
            Log.e(TAG, "Exception log failed!", e);
        }

    }

    private String getExceptionDetails(Throwable ex) {
        StringBuilder sb = new StringBuilder(ex.toString() + "\n");
        for (StackTraceElement message : ex.getStackTrace()) {
            sb.append(message.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 收集设备参数信息
     * @param ctx
     */
    public String collectDeviceInfo(Context ctx) {
        StringBuilder sb = new StringBuilder();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                sb.append("\nversionName ");
                sb.append(versionName);
                sb.append("\nversionCode");
                sb.append(versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occurred when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                sb.append("\n");
                sb.append(field.getName());
                sb.append(" : ");
                sb.append(field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "an error occurred when collect crash info", e);
            }
        }
        return sb.toString();
    }

}