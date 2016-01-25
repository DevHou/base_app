package com.houlijiang.common.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Created by houlijiang on 2014/9/20.
 * 
 * json相关操作，只是对gson的包装
 */
public class JsonUtils {

    private static final String TAG = JsonUtils.class.getSimpleName();

    // 默认所有日期格式都是一样的
    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static <T> T parseString(String result, Class<T> classOfT) {
        if (result == null || classOfT == null) {
            return null;
        }
        try {
            return gson.fromJson(result, classOfT);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "catch exception when format json str:" + result);
            throw e;
        }
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return "";
        }
        return gson.toJson(obj);
    }
}
