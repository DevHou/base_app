package com.common.network;

import java.io.File;
import java.util.Map;

/**
 * Created by houlijiang on 14/11/18.
 *
 * 网络接口，下面不管使用哪个库都要封装成这样的接口
 */
public interface IHttpWorker {

    /**
     * get 请求
     *
     * @param context 绑定的对象 取消时用的
     * @param url url
     * @param header header
     * @param params 参数
     * @param classOfT 返回值类型
     * @param handler 回调
     * @param param 自定义参数
     * @param <Result> 结果
     */
    <Result extends HttpResponseResult> INetCall doGet(Object context, String url, Map<String, String> header,
        IHttpParams params, final Class<Result> classOfT, IHttpResponse<Result> handler, Object param);

    /**
     * get 请求
     *
     * @param context 绑定的对象 取消时用的
     * @param url url
     * @param header header
     * @param params 参数
     * @param classOfT 返回值类型
     * @param handler 回调
     * @param timeout 超时时间
     * @param param 自定义参数
     * @param <Result> 结果
     */
    <Result extends HttpResponseResult> INetCall doGet(Object context, String url, Map<String, String> header,
        IHttpParams params, final Class<Result> classOfT, IHttpResponse<Result> handler, int timeout, Object param);

    /**
     * post请求
     * 
     * @param context 绑定的对象
     * @param url url
     * @param params 参数
     * @param contentType post数据类型
     * @param classOfT 返回值类型
     * @param headers 自定义http头
     * @param handler 回调
     * @param param 自定义参数
     * @param <Result> 结果
     */
    <Result extends HttpResponseResult> INetCall doPost(Object context, String url, IHttpParams params,
        String contentType, Map<String, String> headers, final Class<Result> classOfT, IHttpResponse<Result> handler,
        Object param);

    /**
     * post请求
     *
     * @param context 绑定的对象
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
    <Result extends HttpResponseResult> INetCall doPost(Object context, String url, IHttpParams params,
        String contentType, Map<String, String> headers, final Class<Result> classOfT, IHttpResponse<Result> handler,
        int timeout, Object param);

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
    INetCall download(Object context, String url, Map<String, String> header, File file, IHttpParams params,
        IHttpResponse<File> handler, Object param);

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
    INetCall download(Object context, String url, Map<String, String> header, File file, IHttpParams params,
        IHttpResponse<File> handler, int timeout, Object param);

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
    <Result extends HttpResponseResult> INetCall upload(Object context, String url, Map<String, String> headers,
        Map<String, FileWrapper> files, IHttpParams params, final Class<Result> classOfT,
        IHttpResponse<Result> handler, Object param);

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
    <Result extends HttpResponseResult> INetCall upload(Object context, String url, Map<String, String> headers,
        Map<String, FileWrapper> files, IHttpParams params, final Class<Result> classOfT,
        IHttpResponse<Result> handler, int timeout, Object param);

    /**
     * 取消context相关的所有请求
     * 
     * @param context context
     */
    void cancel(Object context);

    /**
     * 取消所有请求
     */
    void cancelAll();

}
