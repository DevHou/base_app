package com.common.app.base.api;

import android.support.annotation.NonNull;

/**
 * Created by houlijiang on 15/11/28.
 *
 * api层处理后的回调
 */
public interface IApiCallback {

    /**
     * API层回调
     *
     * @param result API层处理的结果
     */
    void onRequestCompleted(@NonNull ApiResultModel result, Object param);
}
