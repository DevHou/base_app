package com.common.app.uikit;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by houlijiang on 16/1/26.
 * 
 * Toast包装的工具类
 */
public class Tips {

    private static final String TAG = Tips.class.getSimpleName();

    private static Toast toast = null;
    private static final Object synObj = new Object();

    /**
     * Toast发送消息，默认Toast.LENGTH_SHORT
     */
    public static void showMessage(final Context act, final String msg) {
        showMessage(act, msg, Toast.LENGTH_SHORT);
    }

    /**
     * Toast发送消息，默认Toast.LENGTH_LONG
     */
    public static void showMessageLong(final Context act, final String msg) {
        showMessage(act, msg, Toast.LENGTH_LONG);
    }

    /**
     * Toast发送消息，默认Toast.LENGTH_SHORT
     */
    public static void showMessage(final Context act, final int msg) {
        showMessage(act, msg, Toast.LENGTH_SHORT);
    }

    /**
     * Toast发送消息，默认Toast.LENGTH_LONG
     */
    public static void showMessageLong(final Context act, final int msg) {
        showMessage(act, msg, Toast.LENGTH_LONG);
    }

    /**
     * 如果已经在显示就不显示了
     */
    public static void showDeadlineMessage(final Context act, final String msg) {
        Log.d(TAG, "value:" + (toast == null) + " " + (toast == null || toast.getView() == null) + " "
            + (toast == null || toast.getView() == null || toast.getView().isShown()));
        if (toast == null || toast.getView() == null || !toast.getView().isShown()) {
            showMessage(act, msg);
        }
    }

    /**
     * Toast发送消息
     */
    public static void showMessage(final Context act, final int msg, final int len) {
        synchronized (synObj) {
            if (act == null) {
                return;
            }
            if (toast != null) {
                // toast.cancel();
                toast.setText(msg);
                toast.setDuration(len);
            } else {
                toast = Toast.makeText(act, msg, len);
            }
            toast.show();
        }
    }

    /**
     * Toast发送消息
     */
    public static void showMessage(final Context act, final String msg, final int len) {
        synchronized (synObj) {
            if (act == null) {
                return;
            }
            if (toast != null) {
                // toast.cancel();
                toast.setText(msg);
                toast.setDuration(len);
            } else {
                toast = Toast.makeText(act, msg, len);
            }
            toast.show();
        }
    }

    /**
     * 关闭当前Toast
     */
    public static void cancelCurrentToast() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
