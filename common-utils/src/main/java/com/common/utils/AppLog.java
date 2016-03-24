package com.common.utils;

import android.util.Log;

/**
 * Created by houlijiang on 16/3/24.
 * 
 * 统一处理log
 */
public class AppLog {

    private static boolean isOnline = true;

    public static void setIsOnline(boolean b) {
        isOnline = b;
    }

    public static void v(String tag, String msg) {
        if (!isOnline)
            Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (!isOnline)
            Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable t) {
        if (!isOnline)
            Log.d(tag, msg, t);
    }

    public static void i(String tag, String msg) {
        if (!isOnline)
            Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (!isOnline)
            Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (!isOnline)
            Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable e) {
        if (!isOnline)
            Log.e(tag, msg, e);
    }
}
