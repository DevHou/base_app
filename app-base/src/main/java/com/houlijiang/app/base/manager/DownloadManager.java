package com.houlijiang.app.base.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.houlijiang.app.base.utils.AppLog;
import com.houlijiang.common.network.HttpResponseError;
import com.houlijiang.common.network.HttpWorker;
import com.houlijiang.common.network.IHttpParams;
import com.houlijiang.common.network.IHttpResponse;
import com.houlijiang.common.utils.DispatchUtils;
import com.houlijiang.common.utils.ResourceManager;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by houlijiang on 16/1/23.
 *
 * 下载管理
 * 回调在主线程
 */
public class DownloadManager {

    private static final String TAG = DownloadManager.class.getSimpleName();

    private static final int HANDLER_DOWNLOAD = 1;
    private boolean mDownload = true;
    private Set<String> mDownloadFilter = new HashSet<>();
    private LinkedBlockingQueue<DownloadItem> mDownloadQueue = new LinkedBlockingQueue<>();
    private HandlerThread mDownloadThread = new HandlerThread("t_files_download");
    private Handler mDownloadHandler;

    private static class InstanceHolder {
        public final static DownloadManager instance = new DownloadManager();
    }

    public static DownloadManager getInstance() {
        return InstanceHolder.instance;
    }

    public void init() {
        mDownload = true;
        mDownloadThread.start();
        mDownloadHandler = new Handler(mDownloadThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                try {
                    DownloadItem item = mDownloadQueue.poll(2000, TimeUnit.MILLISECONDS);
                    if (item != null) {
                        AppLog.v(TAG, "download url:" + item.url);
                        Map<String, String> maps = null;
                        IHttpParams params = null;
                        if (item.paramsCreator != null) {
                            try {
                                maps = item.paramsCreator.createReqHeader(item);
                            } catch (Exception e) {
                                Log.e(TAG, "create req header error, e:" + e.getLocalizedMessage());
                            }
                            try {
                                params = item.paramsCreator.createReqParams(item);
                            } catch (Exception e) {
                                Log.e(TAG, "create req params error, e:" + e.getLocalizedMessage());
                            }
                        }
                        HttpWorker.download(item.origin, item.url, maps, item.target, params,
                            new IHttpResponse<File>() {
                                @Override
                                public void onSuccess(@NonNull File file, final Object param) {
                                    DispatchUtils.getInstance().postInMain(new Runnable() {
                                        @Override
                                        public void run() {
                                            DownloadItem di = (DownloadItem) param;
                                            if (di.isCanceled) {
                                                return;
                                            }
                                            if (di.callback != null) {
                                                try {
                                                    di.callback.onFinish(true, di.param);
                                                } catch (Exception e) {
                                                    AppLog.e(TAG, "callback error, e:" + e.getLocalizedMessage());
                                                }
                                            }
                                            AppLog.v(TAG, "download success url:" + di.url);
                                        }
                                    });
                                }

                                @Override
                                public void onFailed(@NonNull HttpResponseError error, Object param) {
                                    // 失败了就再加入队列
                                    DownloadItem di = (DownloadItem) param;
                                    // 删除以免下次下载时误以为文件存在不下载了
                                    if (di.target != null && di.target.exists()) {
                                        di.target.delete();
                                    }
                                    if (di.isCanceled) {
                                        return;
                                    }
                                    addToDownloadQueueForce(di);
                                }

                                @Override
                                public void onProgress(final long donebytes, final long totalbytes, final Object param) {
                                    DispatchUtils.getInstance().postInMain(new Runnable() {
                                        @Override
                                        public void run() {
                                            DownloadItem di = (DownloadItem) param;
                                            if (di.isCanceled) {
                                                return;
                                            }
                                            if (di.callback != null) {
                                                try {
                                                    di.callback.progress(donebytes, totalbytes, di.param);
                                                } catch (Exception e) {
                                                    AppLog.e(TAG, "callback error, e:" + e.getLocalizedMessage());
                                                }
                                            }
                                        }
                                    });
                                }
                            }, item);
                    }
                } catch (InterruptedException e) {
                    AppLog.e(TAG, "interrupted exception when download");
                }
                if (mDownload) {
                    mDownloadHandler.obtainMessage(HANDLER_DOWNLOAD).sendToTarget();
                }
                return true;
            }
        });
        mDownloadHandler.obtainMessage(HANDLER_DOWNLOAD).sendToTarget();
    }

    /**
     * 释放资源
     */
    public void release() {
        mDownload = false;
        mDownloadHandler.removeCallbacksAndMessages(null);
        mDownloadThread.quit();
    }

    /**
     * 添加需要下载的文件到下载队列
     *
     * @param item 要下载的文件
     */
    public void addToDownloadQueue(DownloadItem item) {
        if (item == null || mDownloadFilter.contains(item.url)) {
            return;
        }
        item.retry = 1;
        if (item.maxRetryTimes <= 0) {
            item.maxRetryTimes = 3;
        }
        mDownloadQueue.offer(item);
        mDownloadFilter.add(item.url);
        // 加入资源管理中，如果item的origin被回收则该下载请求也被取消
        if (item.origin != null) {
            ResourceManager.getInstance().addRequest(item.origin, item);
        } else {
            ResourceManager.getInstance().addRequest(item);
        }
        AppLog.v(TAG, "add url to queue, url:" + item.url);
    }

    private void addToDownloadQueueForce(final DownloadItem item) {
        if (item == null) {
            return;
        }
        // 重试超过最大重试次就不试了
        item.retry++;
        if (item.retry > item.maxRetryTimes) {
            if (item.callback != null) {
                DispatchUtils.getInstance().postInMain(new Runnable() {
                    @Override
                    public void run() {
                        item.callback.onFinish(false, item.param);
                    }
                });
            }
            return;
        }
        mDownloadQueue.offer(item);
        AppLog.v(TAG, "add url back to queue, url:" + item.url);
    }

    public static class DownloadItem implements ResourceManager.Cancelable {
        public Object origin;// 资源对象，当该对象被回收后，则此下载请求也取消，如果为null，则下载一直不会取消直到程序退出
        public String url;// 下载资源url
        public File target;// 下载保存的文件
        public Object param;// 自定义参数，回调时会带上
        public int maxRetryTimes;// 最大重试次数
        public DownloadCallback callback;// 下载回调
        public DownloadParamsCreator paramsCreator;// 下载参数构造器

        private int retry;// 重试次数
        private boolean isCanceled = false;// 是否取消

        @Override
        public boolean cancel() {
            isCanceled = true;
            AppLog.v(TAG, "item:" + url + " cancel");
            return true;
        }
    }

    public interface DownloadParamsCreator {
        /**
         * 构建请求header
         */
        Map<String, String> createReqHeader(DownloadItem di);

        /**
         * 构建请求参数
         */
        IHttpParams createReqParams(DownloadItem di);
    }

    public interface DownloadCallback {

        /**
         * 下载回调
         *
         * @param success 成功与否
         */
        void onFinish(boolean success, Object param);

        /**
         * 下载进度
         * 
         * @param done 完成字节数
         * @param total 同公的字节数
         */
        void progress(long done, long total, Object param);
    }
}
