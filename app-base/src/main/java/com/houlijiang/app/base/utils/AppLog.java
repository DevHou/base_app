package com.houlijiang.app.base.utils;

import android.util.Log;

/**
 * Created by houlijiang on 16/1/22.
 * 
 * 自定义log输出，方便以后扩展
 */
public class AppLog {

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

}
