package com.houlijiang.common.network;

/**
 * Created by houlijiang on 14-9-13.
 * 
 * 网络处理回调接口
 * 如果创建handler时传了param参数，则一定要重载 如下函数
 * onSuccess(Result result, Object param)
 * onFailed(ServerErrorModel error, Object param)
 */
public abstract class AsyncHttpInterface<Result> implements IHttpResponse<Result> {

    public abstract void onSuccess(Result result, Object param);

    public abstract void onFailed(HttpResponseError error, Object param);

    public void onProgress(long donebytes, long totalbytes) {
    }
}
