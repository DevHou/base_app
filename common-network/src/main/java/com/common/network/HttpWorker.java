package com.common.network;

import android.content.Context;

import com.common.network.volley.HttpParams;
import com.common.network.volley.VolleyHttpWorker;
import com.common.utils.JsonUtils;

import java.io.File;
import java.util.Map;

/**
 * Created by houlijiang on 14/11/18.
 *
 * 统一入口，具体实现可以使用各种开源库包装
 */
public class HttpWorker {

    private static IHttpWorker mHttpWorker;

    public static IHttpParams createHttpParams() {
        return new HttpParams();
    }

    public static boolean init(Context context, File cache) {
        mHttpWorker = new VolleyHttpWorker(context, cache);
        return true;
    }

    public static boolean init(Context context, File cache, int timeoutMs) {
        mHttpWorker = new VolleyHttpWorker(context, cache, timeoutMs);
        return true;
    }

    /**
     * 对结果进行统一处理
     *
     * @param result 结果字符串
     * @param clazz 需要转换成的类
     * @param <T> 结果对象类型
     * @return 结果对象
     */
    public static <T extends HttpResponseResult> T handlerResult(String result, Class<T> clazz) {
        if (HttpStringResponse.class.isAssignableFrom(clazz)) {
            HttpStringResponse resp = new HttpStringResponse();
            resp.data = result;
            return (T) resp;
        } else {
            return JsonUtils.parseString(result, clazz);
        }
    }

    /**
     * get 请求
     *
     * @param tag tag
     * @param url url
     * @param params 参数
     * @param classOfT 返回值类型
     * @param handler 回调
     * @param param 自定义参数
     * @param <Result> 结果
     */
    public static <Result extends HttpResponseResult> INetCall get(Object tag, String url, IHttpParams params,
        final Class<Result> classOfT, IHttpResponse<Result> handler, Object param) {
        return mHttpWorker.doGet(tag, url, null, params, classOfT, handler, param);
    }

    /**
     * get 请求
     *
     * @param tag tag
     * @param url url
     * @param header header
     * @param params 参数
     * @param classOfT 返回值类型
     * @param handler 回调
     * @param param 自定义参数
     * @param <Result> 结果
     */
    public static <Result extends HttpResponseResult> INetCall get(Object tag, String url, Map<String, String> header,
        IHttpParams params, final Class<Result> classOfT, IHttpResponse<Result> handler, Object param) {
        return mHttpWorker.doGet(tag, url, header, params, classOfT, handler, param);
    }

    /**
     * post请求
     *
     * @param tag tag
     * @param url url
     * @param params 参数
     * @param contentType post数据类型
     * @param classOfT 返回值类型
     * @param headers 自定义http头
     * @param handler 回调
     * @param param 自定义参数
     * @param <Result> 结果
     */
    public static <Result extends HttpResponseResult> INetCall post(Object tag, String url, IHttpParams params,
        String contentType, Map<String, String> headers, final Class<Result> classOfT, IHttpResponse<Result> handler,
        Object param) {
        return mHttpWorker.doPost(tag, url, params, contentType, headers, classOfT, handler, param);
    }

    /**
     * 下载文件
     *
     * @param tag tag
     * @param url url
     * @param file 下载的文件存储位置
     * @param params 参数
     * @param handler 回调
     * @param param 自定义参数
     */
    public static INetCall download(Object tag, String url, Map<String, String> header, File file, IHttpParams params,
        IHttpResponse<File> handler, Object param) {
        Object t = null;
        if (tag != null) {
            t = tag.hashCode();
        }
        return mHttpWorker.download(t, url, header, file, params, handler, param);
    }

    /**
     * 上传
     *
     * @param tag tag
     * @param url url
     * @param headers 自定义http头
     * @param files 上传的文件
     * @param params 参数
     * @param classOfT 返回值类型
     * @param handler 回调
     * @param param 自定义参数
     * @param <Result> 结果
     */
    public static <Result extends HttpResponseResult> INetCall upload(Object tag, String url,
        Map<String, String> headers, Map<String, FileWrapper> files, IHttpParams params, final Class<Result> classOfT,
        IHttpResponse<Result> handler, Object param) {
        Object t = null;
        if (tag != null) {
            t = tag.hashCode();
        }
        return mHttpWorker.upload(t, url, headers, files, params, classOfT, handler, param);
    }

    /**
     * 取消tag相关的所有请求
     *
     * @param tag tag
     */
    public static void cancel(Object tag) {
        mHttpWorker.cancel(tag);
    }

    /**
     * 取消所有请求
     */
    public static void cancelAll() {
        mHttpWorker.cancelAll();
    }
}
