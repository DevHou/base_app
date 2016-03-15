package com.common.app.base.error;

import android.content.Context;

import com.common.app.base.utils.AppLog;
import com.common.app.base.R;


/**
 * Created by hyj on 11/5/15.
 * 
 * 所有错误都需要定义在这里，同时包含每个错误对应的文字说明
 */
public class ErrorConst {

    private static final String TAG = ErrorConst.class.getSimpleName();

    private static Context context;

    public static void init(Context context) {
        ErrorConst.context = context;
    }

    // =============公用的=====================
    /**
     * 成功，各层公用
     */
    public static final long ERROR_CODE_SUCCESS = 0;
    /**
     * 未知错误，各层公用
     */
    public static final long ERROR_UNKNOWN = -1;
    // ============= 网络层错误=================
    public static final long ERROR_CODE_SUCCESS_EMPTY = 1012020001;
    public static final long ERROR_CODE_FAIL = 1012020002;
    public static final long ERROR_CODE_NETWORK_FAIL = 1012020003;
    public static final long ERROR_CODE_NETWORK_CACHE_ERROR = 1012020004;
    public static final long ERROR_CODE_NETWORK_GET_RESULT_ERROR = 1012020004;
    public static final long ERROR_CODE_JSON_PARSE = 1012021006;
    public static final long ERROR_CODE_JSON_NO_CODE = 1012021007;
    public static final long ERROR_CODE_CALL_BACK = 1012021008;

    public static final long ERROR_CODE_CANCEL = 1012020009;
    /**
     * 网络没有连接
     */
    public static final long ERROR_CODE_NETWORK_DISCONNECTION = 1012020010;

    /**
     * 网络请求url空
     */
    public static final long ERROR_CODE_NETWORK_URL_EMPTY = 1012020011;

    /**
     * 网络请求MediaType为空
     */
    public static final long ERROR_CODE_NETWORK_MEDIA_TYPE_EMPTY = 1012020012;
    // =====================API层错误=====================
    public static final long ERROR_CODE_API_PARAMS_ERROR = 1012020013;
    // =====================service层错误=====================
    public static final long ERROR_SERVICE_JSON_EMPTY = 1012021014;
    // =====================UI层错误=====================

    // =====================其他错误=====================
    public static final long ERROR_CODE_RUNTIME_NO_METHOD = 1012020015;
    public static final long ERROR_CODE_RUNTIME_INVOCATION = 1012020016;
    public static final long ERROR_CODE_RUNTIME_ILLEGAL_ACCESS = 1012020017;

    public static String getMessage(long code) {
        if (ErrorConst.ERROR_CODE_SUCCESS == code) {
            return getString(R.string.TX_ERROR_CODE_SUCCESS);
        } else if (ErrorConst.ERROR_CODE_NETWORK_FAIL == code) {
            return getString(R.string.TX_ERROR_CODE_NETWORK_FAIL);
        } else if (ErrorConst.ERROR_CODE_JSON_PARSE == code) {
            return getString(R.string.TX_ERROR_CODE_JSON_PARSE);
        } else if (ErrorConst.ERROR_CODE_API_PARAMS_ERROR == code) {
            return getString(R.string.TX_ERROR_CODE_API_PARAMS_ERROR);
        } else if (ErrorConst.ERROR_SERVICE_JSON_EMPTY == code) {
            return getString(R.string.TX_ERROR_SERVICE_JSON_EMPTY);
        } else if (ErrorConst.ERROR_CODE_NETWORK_DISCONNECTION == code) {
            return getString(R.string.TX_ERROR_CODE_NETWORK_DISCONNECTION);
        } else if (ErrorConst.ERROR_CODE_CALL_BACK == code) {
            return getString(R.string.TX_ERROR_CODE_CALL_BACK);
        } else if (ErrorConst.ERROR_CODE_CANCEL == code) {
            return getString(R.string.TX_ERROR_CODE_CANCEL);
        } else if (ErrorConst.ERROR_CODE_NETWORK_CACHE_ERROR == code) {
            return getString(R.string.TX_ERROR_CODE_NETWORK_CACHE_ERROR);
        } else if (ErrorConst.ERROR_CODE_NETWORK_GET_RESULT_ERROR == code) {
            return getString(R.string.TX_ERROR_CODE_NETWORK_GET_RESULT_ERROR);
        } else if (ErrorConst.ERROR_CODE_NETWORK_MEDIA_TYPE_EMPTY == code) {
            return getString(R.string.TX_ERROR_CODE_NETWORK_MEDIA_TYPE_EMPTY);
        } else if (ErrorConst.ERROR_CODE_NETWORK_URL_EMPTY == code) {
            return getString(R.string.TX_ERROR_CODE_NETWORK_URL_EMPTY);
        } else if (ErrorConst.ERROR_CODE_RUNTIME_ILLEGAL_ACCESS == code) {
            return getString(R.string.TX_ERROR_CODE_RUNTIME_ILLEGAL_ACCESS);
        } else if (ErrorConst.ERROR_CODE_RUNTIME_INVOCATION == code) {
            return getString(R.string.TX_ERROR_CODE_RUNTIME_INVOCATION);
        } else if (ErrorConst.ERROR_CODE_RUNTIME_NO_METHOD == code) {
            return getString(R.string.TX_ERROR_CODE_RUNTIME_NO_METHOD);
        } else {
            return getString(R.string.TX_ERROR_CODE_NETWORK_FAIL);
        }
    }

    private static String getString(int stringId) {
        if (context == null) {
            AppLog.e(TAG, "error const not init!!!");
            return "";
        }
        return context.getString(stringId);
    }

}
