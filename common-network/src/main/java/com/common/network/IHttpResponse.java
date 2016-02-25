package com.common.network;

import android.support.annotation.NonNull;

/**
 * Created by houlijiang on 14/11/18.
 * 网络回调接口
 */
public interface IHttpResponse<Result> {

    void onSuccess(@NonNull Result result, Object param);

    void onFailed(@NonNull HttpResponseError error, Object param);

    void onProgress(long donebytes, long totalbytes, Object param);
}
