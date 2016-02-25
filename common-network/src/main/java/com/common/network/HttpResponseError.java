package com.common.network;

import android.text.TextUtils;

/**
 * Created by houlijiang on 2014/9/20.
 *
 * http错误包装类
 */
public class HttpResponseError {

    /**
     * 网络错误号列表，自定义的
     */
    public static final int ERROR_PARSE = 501;
    public static final int ERROR_TIMEOUT = 600;
    public static final int ERROR_SERVER_ERROR = 700;
    public static final int ERROR_AUTH = 800;
    public static final int ERROR_AUTH_FILTER = 850;
    public static final int ERROR_CUSTOM_PROCESS = 900;
    public static final int ERROR_UNKNOWN = 950;
    public static final int ERROR_URL_INVALID = 1000;

    /**
     * 网络错误号，供参考
     */
    private int code;
    /**
     * 错误原因，文字描述
     */
    private String reason;
    /**
     * 服务器返回数据
     */
    private String response;

    public HttpResponseError(int code) {
        this.code = code;
        this.reason = "";
    }

    public HttpResponseError(int code, String msg) {
        this.code = code;
        reason = msg;
    }

    public String getReason() {
        if (TextUtils.isEmpty(reason)) {
            switch (code) {
                case ERROR_PARSE: {
                    reason = "数据解析错误";
                    break;
                }
                case ERROR_TIMEOUT: {
                    reason = "连接超时";
                    break;
                }
                case ERROR_SERVER_ERROR: {
                    reason = "服务器错误";
                    break;
                }
                case ERROR_AUTH: {
                    reason = "认证失败";
                    break;
                }
                case ERROR_AUTH_FILTER: {
                    reason = "授权失败";
                    break;
                }
                case ERROR_CUSTOM_PROCESS: {
                    reason = "数据处理错误";
                    break;
                }
                case ERROR_UNKNOWN: {
                    reason = "未知错误";
                    break;
                }
                case ERROR_URL_INVALID: {
                    reason = "网址无效";
                    break;
                }
            }
        }
        return reason;
    }

    public int getCode() {
        return code;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
