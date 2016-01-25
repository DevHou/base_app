package com.houlijiang.app.base.service;

import com.houlijiang.app.base.error.ErrorConst;

/**
 * Created by houlijiang on 11/6/15.
 *
 * 数据层对上层返回的数据
 * 如果成功，code message是成功，结果的result直接通过接口返回
 * 如果失败，code message是服务器返回的错误码和对应文本
 */
public class DataServiceResultModel {

    private static final String TAG = DataServiceResultModel.class.getSimpleName();

    public long code;
    public String message;

    public DataServiceResultModel(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public static DataServiceResultModel create(long code, String message) {
        return new DataServiceResultModel(code, message);
    }

    public static DataServiceResultModel create(long code) {
        return create(code, ErrorConst.getMessage(code));
    }

    @Override
    public String toString() {
        return new StringBuffer(TAG).append(" [code:").append(code).append(", msg:").append(message).append("]")
            .toString();
    }
}
