package com.common.app.base.api;

/**
 * Created by houlijiang on 15/11/28.
 *
 * API层对上层返回的数据
 */
public class ApiResultModel {

    private static final String TAG = ApiResultModel.class.getSimpleName();

    public long code;
    public long time;
    public String message;
    public String result;

    @Override
    public String toString() {
        return new StringBuffer(TAG).append(" [code:").append(code).append(", msg:").append(message).append("]")
            .toString();
    }
}
