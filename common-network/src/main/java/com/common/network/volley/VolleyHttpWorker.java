package com.common.network.volley;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.custom.SingleVolleyClient;
import com.android.volley.toolbox.HttpHeaderParser;
import com.common.network.FileWrapper;
import com.common.network.HttpResponseError;
import com.common.network.HttpResponseResult;
import com.common.network.HttpWorker;
import com.common.network.IHttpParams;
import com.common.network.IHttpResponse;
import com.common.network.IHttpWorker;
import com.common.network.INetCall;
import com.common.utils.AppLog;
import com.common.utils.DispatchUtils;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

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
 * 
 * 普通请求支持按tag cancel，但上传下载没有支持，可以由调用者自己通过INetCall控制
 */
public class VolleyHttpWorker implements IHttpWorker {

    private static final String TAG = VolleyHttpWorker.class.getSimpleName();

    // 连接超时默认时间
    private static final int SOCKET_TIME_OUT = 3000;
    private static final int MAX_RETRY_TIMES = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;

    private int mTimeout;
    private OkHttpClient mHttpClient;

    private X509TrustManager trustManager;
    private SSLSocketFactory sslSocketFactory;

    public VolleyHttpWorker(Context context, File cache) {
        this(context, cache, SOCKET_TIME_OUT);
    }

    public VolleyHttpWorker(Context context, File cache, int timeout) {
        this(context, cache, null, timeout);
    }

    public VolleyHttpWorker(Context context, File cache, InputStream[] certificate, int timeout) {
        mTimeout = timeout;

        if (certificate != null) {
            initForHttps(certificate);
        }

        OkHttpClient.Builder builder =
            new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS).writeTimeout(timeout, TimeUnit.MILLISECONDS);
        if (trustManager != null && sslSocketFactory != null) {
            builder.sslSocketFactory(sslSocketFactory, trustManager);
            builder.hostnameVerifier(new NoneHostnameVerifier());
        }
        mHttpClient = builder.build();
        SingleVolleyClient.getInstance().init(context, cache, mHttpClient, null);
    }

    /**
     * 初始化https
     *
     * @param certificates cer输入文件
     */
    private boolean initForHttps(InputStream[] certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                    AppLog.e(TAG, "close certificate e:" + e.getLocalizedMessage());
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                AppLog.e(TAG, "Unexpected default trust managers:" + Arrays.toString(trustManagers));
                return false;
            } else {
                trustManager = (X509TrustManager) trustManagers[0];
            }
            sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            AppLog.e(TAG, "exception when init ssl, e:" + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * get 请求
     *
     * @param origin 绑定的对象 取消时用的
     * @param url url
     * @param header header
     * @param params 参数
     * @param classOfT 返回值类型
     * @param handler 回调
     * @param timeout 超时时间
     * @param param 自定义参数
     * @param <Result> 结果
     */
    @Override
    public <Result extends HttpResponseResult> INetCall doGet(final Object origin, String url,
        Map<String, String> header, IHttpParams params, final Class<Result> classOfT,
        final IHttpResponse<Result> handler, int timeout, final Object param) {
        if (params != null && params.getParams() != null && params.getParams().size() > 0) {
            boolean first = true;
            try {
                Uri uri = Uri.parse(url);
                if (uri.getQuery() != null) {
                    first = false;
                }
            } catch (Exception e) {
                AppLog.e(TAG, "parse url e:" + e.getLocalizedMessage());
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, String> kv : params.getParams().entrySet()) {
                try {
                    stringBuilder.append(first ? "?" : "&").append(kv.getKey()).append("=")
                        .append(URLEncoder.encode(kv.getValue(), HTTP.UTF_8));
                    first = false;
                } catch (UnsupportedEncodingException e) {
                    AppLog.e(TAG, "create url params e:" + e.getLocalizedMessage());
                }
            }
            url += stringBuilder.toString();
        }

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
                                        AppLog.e(TAG, "get call success e:" + e.getLocalizedMessage());
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
                                        AppLog.e(TAG, "get call error e:" + e.getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    }
                });
        req.setRetryPolicy(new DefaultRetryPolicy(timeout, MAX_RETRY_TIMES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingleVolleyClient.getInstance().addToRequestQueue(req);
        return new RequestCall(req);
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
    public <Result extends HttpResponseResult> INetCall doGet(final Object origin, String url,
        Map<String, String> header, IHttpParams params, Class<Result> classOfT, final IHttpResponse<Result> handler,
        final Object param) {
        return doGet(origin, url, header, params, classOfT, handler, mTimeout, param);
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
     * @param timeout 超时时间
     * @param param 自定义参数
     * @param <Result> 结果
     */
    public <Result extends HttpResponseResult> INetCall doPost(final Object origin, String url, IHttpParams params,
        String contentType, Map<String, String> headers, final Class<Result> classOfT,
        final IHttpResponse<Result> handler, int timeout, final Object param) {

        GsonRequest<Result> req =
            new GsonRequest<>(Request.Method.POST, origin, url, headers, classOfT, params,
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
                                        AppLog.e(TAG, "post call success e:" + e.getLocalizedMessage());
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
                                        AppLog.e(TAG, "post call fail e:" + e.getLocalizedMessage());
                                    }
                                }
                            });
                        }

                    }
                });
        req.setRetryPolicy(new DefaultRetryPolicy(timeout, MAX_RETRY_TIMES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingleVolleyClient.getInstance().addToRequestQueue(req);
        return new RequestCall(req);
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
    public <Result extends HttpResponseResult> INetCall doPost(final Object origin, String url, IHttpParams params,
        String contentType, Map<String, String> headers, Class<Result> classOfT, final IHttpResponse<Result> handler,
        final Object param) {
        return doPost(origin, url, params, contentType, headers, classOfT, handler, mTimeout, param);
    }

    /**
     * 下载文件
     *
     * @param tag 绑定的对象
     * @param url url
     * @param header header
     * @param file 下载的文件存储位置
     * @param params 参数
     * @param handler 回调
     * @param param 自定义参数
     */
    @Override
    public INetCall download(Object tag, String url, Map<String, String> header, final File file, IHttpParams params,
        final IHttpResponse<File> handler, final Object param) {
        return download(tag, url, header, file, params, handler, SOCKET_TIME_OUT, param);
    }

    /**
     * 下载文件
     *
     * @param tag 绑定的对象
     * @param url url
     * @param header header
     * @param file 下载的文件存储位置
     * @param params 参数
     * @param handler 回调
     * @param param 自定义参数
     */
    @Override
    public INetCall download(Object tag, String url, Map<String, String> header, final File file, IHttpParams params,
        final IHttpResponse<File> handler, int timeout, final Object param) {

        OkHttpClient client;
        if (timeout != SOCKET_TIME_OUT) {
            client =
                mHttpClient.newBuilder().connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS).writeTimeout(timeout, TimeUnit.MILLISECONDS).build();
        } else {
            client = mHttpClient;
        }

        OkHttpClient copy = client.newBuilder().addNetworkInterceptor(new Interceptor() {
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
        } catch (final Exception e) {
            AppLog.e(TAG, "download url invalid, e:" + e.getLocalizedMessage());
            if (handler != null) {
                DispatchUtils.getInstance().postInBackground(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpResponseError error =
                                new HttpResponseError(HttpResponseError.ERROR_URL_INVALID, e.getLocalizedMessage());
                            handler.onFailed(error, param);
                        } catch (Exception e) {
                            AppLog.e(TAG, "download call fail e:" + e.getLocalizedMessage());
                        }
                    }
                });
            }
            return new OkHttpCall(null);
        }

        Call call = copy.newCall(requestBuilder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                AppLog.v(TAG, "download failed, e", e);
                if (handler != null) {
                    DispatchUtils.getInstance().postInBackground(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                file.deleteOnExit();
                                if (call.isCanceled()) {
                                    return;
                                }
                                HttpResponseError error =
                                    new HttpResponseError(HttpResponseError.ERROR_UNKNOWN, e.getLocalizedMessage());
                                handler.onFailed(error, param);
                            } catch (Exception e) {
                                AppLog.e(TAG, "download call fail e:" + e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onResponse(final Call call, okhttp3.Response response) throws IOException {
                try {
                    BufferedSink sink = Okio.buffer(Okio.sink(file));
                    sink.writeAll(response.body().source());
                    sink.close();
                } catch (final Exception e) {
                    AppLog.v(TAG, "copy download file, e", e);
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
                                    AppLog.e(TAG, "download call success e:" + e.getLocalizedMessage());
                                }
                            }
                        });
                    }
                    return;
                }
                if (handler != null && !call.isCanceled()) {
                    DispatchUtils.getInstance().postInBackground(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handler.onSuccess(file, param);
                            } catch (Exception e) {
                                AppLog.e(TAG, "download call success e:" + e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        });
        return new OkHttpCall(call);
    }

    /**
     * 上传
     *
     * @param tag 绑定的对象
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
    public <Result extends HttpResponseResult> INetCall upload(Object tag, String url, Map<String, String> headers,
        Map<String, FileWrapper> files, IHttpParams params, final Class<Result> classOfT,
        final IHttpResponse<Result> handler, final Object param) {
        return upload(tag, url, headers, files, params, classOfT, handler, SOCKET_TIME_OUT, param);
    }

    /**
     * 上传
     *
     * @param tag 绑定的对象
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
    public <Result extends HttpResponseResult> INetCall upload(Object tag, String url, Map<String, String> headers,
        Map<String, FileWrapper> files, IHttpParams params, final Class<Result> classOfT,
        final IHttpResponse<Result> handler, int timeout, final Object param) {

        OkHttpClient client;
        if (timeout != SOCKET_TIME_OUT) {
            client =
                mHttpClient.newBuilder().connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS).writeTimeout(timeout, TimeUnit.MILLISECONDS).build();
        } else {
            client = mHttpClient;
        }

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
        Call call = client.newCall(requestBuilder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                AppLog.v(TAG, "upload failed, e", e);
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
                                AppLog.e(TAG, "upload call fail e:" + e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onResponse(final Call call, final okhttp3.Response response) throws IOException {
                if (handler != null && !call.isCanceled()) {
                    try {
                        DispatchUtils.getInstance().postInBackground(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final Result result = HttpWorker.handlerResult(response.body().string(), classOfT);
                                    handler.onSuccess(result, param);
                                } catch (Exception e) {
                                    AppLog.e(TAG, "upload call success e:" + e.getLocalizedMessage());
                                }
                            }
                        });
                    } catch (Exception e) {
                        AppLog.e(TAG, "parse result for upload e:" + e.getLocalizedMessage());
                    }
                }
            }
        });
        return new OkHttpCall(call);
    }

    /**
     * 取消tag相关的所有请求
     *
     * @param tag tag
     */
    @Override
    public void cancel(Object tag) {
        if (tag != null) {
            SingleVolleyClient.getInstance().getRequestQueue().cancelAll(tag);
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
            try {
                requestBody.writeTo(bufferedSink);
                // 必须调用flush，否则最后一部分数据可能不会被写入
                bufferedSink.flush();
            } catch (Exception e) {
                AppLog.e(TAG, "写入错误，e:" + e.getLocalizedMessage());
            }

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

    private class NoneHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }

    private class AllTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

}
