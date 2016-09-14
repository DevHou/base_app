package com.common.app.base.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.common.network.FileWrapper;
import com.common.network.HttpResponseError;
import com.common.network.HttpResponseResult;
import com.common.network.HttpStringResponse;
import com.common.network.HttpWorker;
import com.common.network.IHttpParams;
import com.common.network.IHttpResponse;
import com.common.network.INetCall;
import com.common.utils.AppLog;
import com.common.utils.DispatchUtils;
import com.common.utils.JsonUtils;
import com.common.utils.ResourceManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by houlijiang on 16/1/23.
 *
 * 文件上传管理
 */
public class UploadManager {

    private static final String TAG = UploadManager.class.getSimpleName();

    private static final int TIMEOUT = 20000;
    private static final int HANDLER_UPLOAD = 1;
    private boolean mUpload = true;
    private Set<String> mUploadFilter = new HashSet<>();
    private LinkedBlockingQueue<UploadItem> mUploadQueue = new LinkedBlockingQueue<>();
    private HandlerThread mUploadThread = new HandlerThread("t_files_upload");
    private Handler mUploadHandler;

    private static class InstanceHolder {
        public final static UploadManager instance = new UploadManager();
    }

    public static UploadManager getInstance() {
        return InstanceHolder.instance;
    }

    public void init() {
        mUpload = true;
        mUploadThread.start();
        mUploadHandler = new Handler(mUploadThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                try {
                    final UploadItem item = mUploadQueue.poll(2000, TimeUnit.MILLISECONDS);
                    if (item != null) {
                        AppLog.v(TAG, "upload url:" + item.url);
                        if (TextUtils.isEmpty(item.name) || item.target == null) {
                            AppLog.v(TAG, "upload name or target is null");
                            DispatchUtils.getInstance().postInMain(new Runnable() {
                                @Override
                                public void run() {
                                    item.callback.onFinish(false, null, item.param);
                                }
                            });
                            return true;
                        }
                        Map<String, String> header = null;
                        IHttpParams params = null;
                        if (item.paramsCreator != null) {
                            try {
                                header = item.paramsCreator.createReqHeader(item);
                            } catch (Exception e) {
                                AppLog.e(TAG, "create req header error, e:" + e.getLocalizedMessage());
                            }
                            try {
                                params = item.paramsCreator.createReqParams(item);
                            } catch (Exception e) {
                                AppLog.e(TAG, "create req params error, e:" + e.getLocalizedMessage());
                            }
                        }
                        Map<String, FileWrapper> files = new HashMap<>();
                        files.put(item.name, item.target);
                        item.call =
                            HttpWorker.upload(item.origin, item.url, header, files, params, HttpStringResponse.class,
                                new IHttpResponse<HttpStringResponse>() {
                                    @Override
                                    public void onSuccess(@NonNull final HttpStringResponse result, final Object param) {
                                        DispatchUtils.getInstance().postInMain(new Runnable() {
                                            @Override
                                            public void run() {
                                                UploadItem di = (UploadItem) param;
                                                if (di.isCanceled) {
                                                    return;
                                                }
                                                AppLog.d(TAG, "result:" + result.data);
                                                if (di.callback != null) {
                                                    try {
                                                        UploadResult uploadResult = new UploadResult();
                                                        uploadResult.result =
                                                            JsonUtils.parseString(result.data, di.resultClass);
                                                        di.callback.onFinish(true, uploadResult, di.param);
                                                    } catch (Exception e) {
                                                        AppLog.e(TAG, "callback error, e:" + e.getLocalizedMessage());
                                                    }
                                                }
                                                AppLog.v(TAG, "upload success url:" + di.url);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailed(@NonNull HttpResponseError error, Object param) {
                                        AppLog.v(TAG, "upload failed, will retry");
                                        // 失败了就再加入队列
                                        UploadItem di = (UploadItem) param;
                                        if (di.isCanceled) {
                                            return;
                                        }
                                        addToUploadQueueForce(di);
                                    }

                                    @Override
                                    public void onProgress(final long donebytes, final long totalbytes,
                                        final Object param) {
                                        AppLog.v(TAG, "done:" + donebytes + " total:" + totalbytes);
                                        DispatchUtils.getInstance().postInMain(new Runnable() {
                                            @Override
                                            public void run() {
                                                UploadItem di = (UploadItem) param;
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
                                }, TIMEOUT, item);
                    }
                } catch (InterruptedException e) {
                    AppLog.e(TAG, "interrupted exception when upload");
                }
                if (mUpload) {
                    mUploadHandler.obtainMessage(HANDLER_UPLOAD).sendToTarget();
                }
                return true;
            }
        });
        mUploadHandler.obtainMessage(HANDLER_UPLOAD).sendToTarget();
    }

    /**
     * 释放资源
     */
    public void release() {
        mUpload = false;
        mUploadHandler.removeCallbacksAndMessages(null);
        mUploadThread.quit();
    }

    /**
     * 添加需要上传的文件到上传队列
     *
     * @param item 要上传的文件
     */
    public void addToUploadQueue(UploadItem item) {
        if (item == null) {
            return;
        }
        item.retry = 1;
        if (item.maxRetryTimes <= 0) {
            item.maxRetryTimes = 3;
        }
        mUploadQueue.offer(item);
        mUploadFilter.add(item.url);
        // 加入资源管理中，如果item的origin被回收则该下载请求也被取消
        if (item.origin != null) {
            ResourceManager.getInstance().addRequest(item.origin, item);
        } else {
            ResourceManager.getInstance().addRequest(item);
        }
        AppLog.v(TAG, "add url to queue, url:" + item.url);
    }

    private void addToUploadQueueForce(final UploadItem item) {
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
                        item.callback.onFinish(false, null, item.param);
                    }
                });
            }
            return;
        }
        mUploadQueue.offer(item);
        AppLog.v(TAG, "add url back to queue, url:" + item.url);
    }

    /**
     * 上传对象
     */
    public static class UploadItem implements ResourceManager.Cancelable {
        public Object origin;// 资源对象，当该对象被回收后，则此下载请求也取消，如果为null，则下载一直不会取消直到程序退出
        public String url;// 上传url
        public String name;// 上传的文件的name，即后台用来取文件的name而不是文件本身的名字
        public FileWrapper target;// 上传的文件
        public Class resultClass;// 返回结果的类
        public Object param;// 自定义参数，回调时会带上
        public int maxRetryTimes;// 最大重试次数
        public UploadCallback callback;// 下载回调
        public UploadParamsCreator paramsCreator;// 下载参数构造器

        protected INetCall call;

        private int retry;// 重试次数
        private boolean isCanceled = false;// 是否取消

        @Override
        public boolean cancel() {
            if (call != null) {
                call.cancel();
            }
            isCanceled = true;
            return true;
        }
    }

    /**
     * 上传返回结果
     */
    public static class UploadResult<Result> extends HttpResponseResult {
        public Result result;
    }

    /**
     * 上传参数构造
     */
    public interface UploadParamsCreator {
        /**
         * 构建请求header
         */
        Map<String, String> createReqHeader(UploadItem di);

        /**
         * 构建请求参数
         */
        IHttpParams createReqParams(UploadItem di);
    }

    /**
     * 上传回调
     */
    public interface UploadCallback {

        /**
         * 上传回调
         *
         * @param success 成功与否
         */
        void onFinish(boolean success, UploadResult result, Object param);

        /**
         * 上传进度
         *
         * @param done 完成字节数
         * @param total 同公的字节数
         */
        void progress(long done, long total, Object param);
    }
}
