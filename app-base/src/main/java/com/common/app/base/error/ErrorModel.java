package com.common.app.base.error;

import android.text.TextUtils;

import com.common.app.base.utils.AppLog;


/**
 * Created by houlijiang on 11/11/15.
 * 
 * 错误统一定义，各层处理错误时使用这个通用类
 */
public class ErrorModel {

    private static String TAG = ErrorModel.class.getSimpleName();

    public long code;
    public String message;
    private Exception exception;

    public static ErrorModel errorWithCode(long code) {
        return ErrorModel.errorWithCode(code, "");
    }

    public static ErrorModel errorWithCode(long code, String msg) {
        ErrorModel error = new ErrorModel();
        error.code = code;
        if (!TextUtils.isEmpty(msg)) {
            error.message = msg;
        } else {
            error.message = ErrorConst.getMessage(code);
        }
        return error;
    }

    public static ErrorModel errorWithCode(long code, Exception exception) {
        ErrorModel error = new ErrorModel();
        error.code = code;
        error.message = ErrorConst.getMessage(code);
        error.exception = exception;
        return error;
    }

    public void print() {
        this.print(TAG);
    }

    public void print(String tag) {
        String eTag = (null == tag || tag.isEmpty()) ? TAG : tag;
        AppLog.e(eTag, String.format("%d: %s.%n%s", this.code, this.message, this.exception.getStackTrace().toString()));
    }
}
