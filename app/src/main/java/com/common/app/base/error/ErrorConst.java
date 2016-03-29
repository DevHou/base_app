package com.common.app.base.error;

import android.content.Context;

import com.common.app.R;
import com.common.utils.AppLog;

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
    // 访问不到服务器
    public static final long ERROR_CODE_NETWORK_FAIL = 1012020001;
    // 授权失败
    public static final long ERROR_CODE_NETWORK_AUTH_FAIL = 1012020002;
    // 网络层解析结果错误
    public static final long ERROR_CODE_NETWORK_PARSE_RESULT_ERROR = 1012020003;
    // 服务器异常
    public static final long ERROR_CODE_NETWORK_SERVER_ERROR = 1012020004;
    // 网络超时
    public static final long ERROR_CODE_NETWORK_TIME_OUT = 1012021005;
    // url不合法
    public static final long ERROR_CODE_NETWORK_URL_INVALID = 1012021006;
    // 回调错误
    public static final long ERROR_CODE_NETWORK_CALL_BACK = 1012021007;
    // 网络取消
    public static final long ERROR_CODE_CANCEL = 1012020008;
    // 网络没有连接
    public static final long ERROR_CODE_NETWORK_DISCONNECTION = 1012020009;
    // 网络请求url空
    public static final long ERROR_CODE_NETWORK_URL_EMPTY = 1012020010;
    // =====================API层错误=====================
    public static final long ERROR_CODE_API_PARAMS_ERROR = 1012020011;
    public static final long ERROR_CODE_API_FAIL = 1012020012;
    // =====================service层错误=====================
    public static final long ERROR_CODE_SERVICE_JSON_EMPTY = 1012021013;
    public static final long ERROR_CODE_SERVICE_PARSE_ERROR = 1012021014;
    // =====================UI层错误=====================

    // =====================其他错误=====================
    public static final long ERROR_CODE_RUNTIME_NO_METHOD = 1012020015;
    public static final long ERROR_CODE_RUNTIME_INVOCATION = 1012020016;
    public static final long ERROR_CODE_RUNTIME_ILLEGAL_ACCESS = 1012020017;

    public static String getMessage(long code) {
        if (ErrorConst.ERROR_CODE_SUCCESS == code) {
            return getString(R.string.error_code_success);
        } else if (ErrorConst.ERROR_CODE_NETWORK_FAIL == code) {
            return getString(R.string.error_code_network_fail);
        } else if (ErrorConst.ERROR_CODE_NETWORK_AUTH_FAIL == code) {
            return getString(R.string.error_code_network_auth_fail);
        } else if (ErrorConst.ERROR_CODE_NETWORK_PARSE_RESULT_ERROR == code) {
            return getString(R.string.error_code_network_parse_result_error);
        } else if (ErrorConst.ERROR_CODE_NETWORK_SERVER_ERROR == code) {
            return getString(R.string.error_code_network_server_error);
        } else if (ErrorConst.ERROR_CODE_NETWORK_TIME_OUT == code) {
            return getString(R.string.error_code_network_time_out);
        } else if (ErrorConst.ERROR_CODE_NETWORK_URL_INVALID == code) {
            return getString(R.string.error_code_network_url_invalid);
        } else if (ErrorConst.ERROR_CODE_NETWORK_CALL_BACK == code) {
            return getString(R.string.error_code_network_callback_error);
        } else if (ErrorConst.ERROR_CODE_CANCEL == code) {
            return getString(R.string.error_code_network_cancel);
        } else if (ErrorConst.ERROR_CODE_NETWORK_DISCONNECTION == code) {
            return getString(R.string.error_code_network_disconnection);
        } else if (ErrorConst.ERROR_CODE_NETWORK_URL_EMPTY == code) {
            return getString(R.string.error_code_network_url_invalid);
        } else if (ErrorConst.ERROR_CODE_API_PARAMS_ERROR == code) {
            return getString(R.string.error_code_api_params_error);
        } else if (ErrorConst.ERROR_CODE_API_FAIL == code) {
            return getString(R.string.error_code_api_error);
        } else if (ErrorConst.ERROR_CODE_SERVICE_JSON_EMPTY == code) {
            return getString(R.string.error_code_service_json_empty);
        } else if (ErrorConst.ERROR_CODE_SERVICE_PARSE_ERROR == code) {
            return getString(R.string.error_code_service_parse_error);
        } else if (ErrorConst.ERROR_CODE_RUNTIME_ILLEGAL_ACCESS == code) {
            return getString(R.string.error_code_runtime_illegal_access);
        } else if (ErrorConst.ERROR_CODE_RUNTIME_INVOCATION == code) {
            return getString(R.string.error_code_runtime_invocation);
        } else if (ErrorConst.ERROR_CODE_RUNTIME_NO_METHOD == code) {
            return getString(R.string.error_code_runtime_no_method);
        } else {
            return getString(R.string.error_code_unknown);
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
