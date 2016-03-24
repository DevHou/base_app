package com.common.app.base.api;

/**
 * Created by houlijiang on 15/11/28.
 *
 * 下载的回调
 */
public interface IDownloadCallback extends IApiCallback {

    /**
     * 下载进度
     * 
     * @param done 完成大小
     * @param total 总大小
     */
    void onDownloadProgress(long done, long total);
}
