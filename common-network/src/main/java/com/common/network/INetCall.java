package com.common.network;

/**
 * Created by houlijiang on 16/3/30.
 * 
 * 请求返回接口，可以用来取消
 */
public interface INetCall {

    /**
     * 取消请求
     */
    void cancel();

    /**
     * 请求是否取消
     * 
     * @return 是否取消
     */
    boolean isCanceled();
}
