package com.houlijiang.common.network.volley;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.jjc.volley.DefaultRetryPolicy;
import com.jjc.volley.Request;
import com.jjc.volley.RequestQueue;
import com.jjc.volley.Response;
import com.jjc.volley.VolleyError;
import com.jjc.volley.custom.SingleVolleyClient;
import com.jjc.volley.toolbox.HttpHeaderParser;
import com.houlijiang.common.network.FileWrapper;
import com.houlijiang.common.network.HttpResponseError;
import com.houlijiang.common.network.HttpResponseResult;
import com.houlijiang.common.network.HttpWorker;
import com.houlijiang.common.network.IHttpParams;
import com.houlijiang.common.network.IHttpResponse;
import com.houlijiang.common.network.IHttpWorker;
import com.houlijiang.common.utils.DispatchUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by houlijiang on 14/11/19.
 *
 * volley是纯内存操作的，所有请求和结果先放在内存再处理，所以在它基础上做上传下载大文件可能出现问题
 * 这里的上传下载用的OkHttp原生处理
 */
public class VolleyHttpWorker implements IHttpWorker {

    private static final String TAG = VolleyHttpWorker.class.getSimpleName();

    // 连接超时默认时间
    private static final int SOCKET_TIME_OUT = 3000;

    private int mTimeout;
    private OkHttpClient mHttpClient;

    public VolleyHttpWorker(Context context, File cache) {
        this(context, cache, SOCKET_TIME_OUT);
    }

    public VolleyHttpWorker(Context context, File cache, int timeout) {
        mTimeout = timeout;
        mHttpClient =
            new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS).writeTimeout(timeout, TimeUnit.MILLISECONDS).build();
        SingleVolleyClient.getInstance().init(context, cache, mHttpClient, null);
    }

    /**
     * get请求
     * 
     * @param origin 绑定的对象
     * @param url 请求地址
     * @param header 请求头
     * @param params 参数列表
     * @param classOfT 返回类型
     * @param handler 回调
     * @param param 自定义参数
     */
    @Override
    public <Result extends HttpResponseResult> void doGet(final Object origin, String url, Map<String, String> header,
        IHttpParams params, Class<Result> classOfT, final IHttpResponse<Result> handler, final Object param) {

        GsonRequest<Result> req =
            new GsonRequest<>(Request.Method.GET, origin, url, header, classOfT, (HttpParams) params,
                new Response.Listener<Result>() {

                    @Override
                    public void onResponse(final Result response) {
                        if (handler != null) {
                            DispatchUtils.getInstance().postInBackground(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        handler.onSuccess(response, param);
                                    } catch (Exception e) {
                                        Log.e(TAG, "get call success e:" + e.getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (handler != null) {
                            DispatchUtils.getInstance().postInBackground(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        HttpResponseError e = VolleyUtils.getResponseError(error);
                                        if (error.networkResponse != null) {
                                            String json;
                                            try {
                                                json =
                                                    new String(error.networkResponse.data, HttpHeaderParser
                                                        .parseCharset(error.networkResponse.headers));
                                            } catch (UnsupportedEncodingException e2) {
                                                json = new String(error.networkResponse.data);
                                            }
                                            e.setResponse(json);
                                        }
                                        handler.onFailed(e, param);
                                    } catch (Exception e) {
                                        Log.e(TAG, "get call error e:" + e.getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    }
                });
        req.setRetryPolicy(new DefaultRetryPolicy(mTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingleVolleyClient.getInstance().addToRequestQueue(req);
    }

    /**
     * post请求
     *
     * @param origin 绑定的对象
     * @param url url
     * @param params 参数
     * @param contentType post数据类型
     * @param classOfT 返回值类型
     * @param headers 自定义http头
     * @param handler 回调
     * @param param 自定义参数
     * @param <Result> 结果
     */
    @Override
    public <Result extends HttpResponseResult> void doPost(final Object origin, String url, IHttpParams params,
        String contentType, Map<String, String> headers, Class<Result> classOfT, final IHttpResponse<Result> handler,
        final Object param) {

        GsonRequest<Result> req =
            new GsonRequest<>(Request.Method.POST, origin, url, headers, classOfT, (HttpParams) params,
                new Response.Listener<Result>() {

                    @Override
                    public void onResponse(final Result response) {
                        if (handler != null) {
                            DispatchUtils.getInstance().postInBackground(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        handler.onSuccess(response, param);
                                    } catch (Exception e) {
                                        Log.e(TAG, "post call success e:" + e.getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (handler != null) {
                            DispatchUtils.getInstance().postInBackground(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        HttpResponseError e = VolleyUtils.getResponseError(error);
                                        if (error.networkResponse != null) {
                                            String json;
                                            try {
                                                json =
                                                    new String(error.networkResponse.data, HttpHeaderParser
                                                        .parseCharset(error.networkResponse.headers));
                                            } catch (UnsupportedEncodingException e2) {
                                                json = new String(error.networkResponse.data);
                                            }
                                            e.setResponse(json);
                                        }
                                        handler.onFailed(e, param);
                                    } catch (Exception e) {
                                        Log.e(TAG, "post call fail e:" + e.getLocalizedMessage());
                                    }
                                }
                            });
                        }

                    }
                });
        req.setRetryPolicy(new DefaultRetryPolicy(mTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingleVolleyClient.getInstance().addToRequestQueue(req);
    }

    /**
     * 下载文件
     *
     * @param context 绑定的对象
     * @param url url
     * @param header header
     * @param file 下载的文件存储位置
     * @param params 参数
     * @param handler 回调
     * @param param 自定义参数
     */
    @Override
    public void download(Object context, String url, Map<String, String> header, final File file, IHttpParams params,
        final IHttpResponse<File> handler, final Object param) {

        OkHttpClient copy = mHttpClient.newBuilder().addNetworkInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                    .body(new ProgressResponseBody<>(originalResponse.body(), handler, param)).build();
            }
        }).build();

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        try {
            requestBuilder.url(url);
        }catch (final Exception e){
            Log.e(TAG,"download url invalid, e:"+e.getLocalizedMessage());
            if (handler != null) {
                DispatchUtils.getInstance().postInBackground(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpResponseError error =
                                    new HttpResponseError(HttpResponseError.ERROR_URL_INVALID, e.getLocalizedMessage());
                            handler.onFailed(error, param);
                        } catch (Exception e) {
                            Log.e(TAG, "download call fail e:" + e.getLocalizedMessage());
                        }
                    }
                });
            }
            return;
        }

        copy.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (handler != null) {
                    DispatchUtils.getInstance().postInBackground(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (call.isCanceled()) {
                                    return;
                                }
                                HttpResponseError error =
                                    new HttpResponseError(HttpResponseError.ERROR_UNKNOWN, e.getLocalizedMessage());
                                handler.onFailed(error, param);
                            } catch (Exception e) {
                                Log.e(TAG, "download call fail e:" + e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    BufferedSink sink = Okio.buffer(Okio.sink(file));
                    sink.writeAll(response.body().source());
                    sink.close();
                } catch (final Exception e) {
                    if (handler != null) {
                        DispatchUtils.getInstance().postInBackground(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    HttpResponseError error =
                                        new HttpResponseError(HttpResponseError.ERROR_UNKNOWN, e.getLocalizedMessage());
                                    handler.onFailed(error, param);
                                } catch (Exception e) {
                                    Log.e(TAG, "download call success e:" + e.getLocalizedMessage());
                                }
                            }
                        });
                    }
                    return;
                }
                if (handler != null) {
                    DispatchUtils.getInstance().postInBackground(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handler.onSuccess(file, param);
                            } catch (Exception e) {
                                Log.e(TAG, "download call success e:" + e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 上传
     *
     * @param context 绑定的对象
     * @param url url
     * @param headers 自定义http头
     * @param files 上传的文件
     * @param params 参数
     * @param classOfT 返回值类型
     * @param handler 回调
     * @param param 自定义参数
     * @param <Result> 结果
     */
    @Override
    public <Result extends HttpResponseResult> void upload(Object context, String url, Map<String, String> headers,
        Map<String, FileWrapper> files, IHttpParams params, final Class<Result> classOfT,
        final IHttpResponse<Result> handler, final Object param) {

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        requestBuilder.url(url);
        // 构建header
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }

        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        // 构建params
        if (params != null && params.getParams() != null) {
            for (Map.Entry<String, String> p : params.getParams().entrySet()) {
                requestBody.addFormDataPart(p.getKey(), p.getValue());
            }
        }
        // 构建文件
        for (Map.Entry<String, FileWrapper> file : files.entrySet()) {
            FileWrapper f = file.getValue();
            String name = file.getKey();
            if (TextUtils.isEmpty(f.customFileName)) {
                f.customFileName = f.file.getName();
            }
            requestBody.addFormDataPart(name, f.customFileName,
                RequestBody.create(MediaType.parse(f.contentType), f.file));
        }

        ProgressRequestBody req = new ProgressRequestBody<>(requestBody.build(), handler, param);
        requestBuilder.post(req);
        mHttpClient.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (handler != null) {
                    DispatchUtils.getInstance().postInBackground(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (call.isCanceled()) {
                                    return;
                                }
                                HttpResponseError error =
                                    new HttpResponseError(HttpResponseError.ERROR_UNKNOWN, e.getLocalizedMessage());
                                handler.onFailed(error, param);
                            } catch (Exception e) {
                                Log.e(TAG, "upload call fail e:" + e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                if (handler != null) {
                    try {
                        DispatchUtils.getInstance().postInBackground(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final Result result = HttpWorker.handlerResult(response.body().string(), classOfT);
                                    handler.onSuccess(result, param);
                                } catch (Exception e) {
                                    Log.e(TAG, "upload call success e:" + e.getLocalizedMessage());
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "parse result for upload e:" + e.getLocalizedMessage());
                    }
                }
            }
        });
    }

    /**
     * 取消context相关的所有请求
     *
     * @param context context
     */
    @Override
    public void cancel(Object context) {
        if (context != null) {
            SingleVolleyClient.getInstance().getRequestQueue().cancelAll(context);
        } else {
            cancelAll();
        }
    }

    /**
     * 取消所有请求
     */
    @Override
    public void cancelAll() {
        // 这里必须保证实例已经创建
        if (SingleVolleyClient.getInstance() == null) {
            return;
        }
        SingleVolleyClient.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    /**
     * 包装的返回体，处理进度
     */
    private static class ProgressResponseBody<Result> extends ResponseBody {

        private final ResponseBody responseBody;
        private final IHttpResponse<Result> progressListener;
        private final Object param; // 回调参数
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, final IHttpResponse<Result> progressListener,
            final Object param) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
            this.param = param;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    // 回调，如果contentLength()不知道长度，会返回-1
                    if (progressListener != null) {
                        DispatchUtils.getInstance().postInMain(new Runnable() {
                            @Override
                            public void run() {
                                progressListener.onProgress(totalBytesRead, responseBody.contentLength(), param);
                            }
                        });
                    }
                    return bytesRead;
                }
            };
        }
    }

    /**
     * 包装的请求体，处理进度
     */
    public class ProgressRequestBody<Result> extends RequestBody {
        // 实际的待包装请求体
        private final RequestBody requestBody;
        // 进度回调接口
        private final IHttpResponse<Result> progressListener;
        // 回调参数
        private final Object param;
        // 包装完成的BufferedSink
        private BufferedSink bufferedSink;

        /**
         * 构造函数，赋值
         * 
         * @param requestBody 待包装的请求体
         * @param progressListener 回调接口
         */
        public ProgressRequestBody(RequestBody requestBody, IHttpResponse<Result> progressListener, Object param) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
            this.param = param;
        }

        /**
         * 重写调用实际的响应体的contentType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                // 包装
                bufferedSink = Okio.buffer(sink(sink));
            }
            // 写入
            requestBody.writeTo(bufferedSink);
            // 必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();

        }

        /**
         * 写入，回调进度接口
         */
        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                // 当前写入字节数
                long bytesWritten = 0L;
                // 总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        // 获得contentLength的值，后续不再调用
                        contentLength = contentLength();
                    }
                    // 增加当前写入的字节数
                    bytesWritten += byteCount;
                    // 回调
                    if (progressListener != null) {
                        DispatchUtils.getInstance().postInBackground(new Runnable() {
                            @Override
                            public void run() {
                                progressListener.onProgress(bytesWritten, contentLength, param);
                            }
                        });
                    }
                }
            };
        }
    }

}
