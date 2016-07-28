package com.common.network.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.common.network.HttpResponseResult;
import com.common.network.HttpWorker;
import com.common.utils.AppLog;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by houlijiang on 14/11/18.
 *
 * volley的自定义请求
 * 如果T是String则直接返回string，否则将结果解析成json
 */
public class GsonRequest<T extends HttpResponseResult> extends Request<T> {

    private static final String TAG = GsonRequest.class.getSimpleName();
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final HttpParams params;
    private final Response.Listener<T> listener;

    /**
     * Make a request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GsonRequest(int method, Object origin, String url, Map<String, String> headers, Class<T> clazz,
        HttpParams params, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.headers = headers;
        this.clazz = clazz;
        this.params = params;
        this.listener = listener;
        setTag(origin);
        setShouldCache(false);

        AppLog.v(TAG, "url:" + url);
        AppLog.v(TAG, "header:");
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                AppLog.v(TAG, "    key:" + entry.getKey() + " \tvalue:" + entry.getValue());
            }
        }
        AppLog.v(TAG, "params:");
        if (params != null && params.getParams() != null && params.getParams().size() > 0) {
            for (Map.Entry<String, String> entry : params.getParams().entrySet()) {
                AppLog.v(TAG, "    key:" + entry.getKey() + " \tvalue:" + entry.getValue());
            }
        }

    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params == null ? null : (params.getParams() != null ? params.getParams() : super.getParams());
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String json;
        try {
            json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            json = new String(response.data);
        }
        AppLog.v(TAG, "url:" + getUrl() + "\nvolley return string:" + json);
        try {
            T result = HttpWorker.handlerResult(json, clazz);
            if (result != null) {
                result.networkTimeMs = response.networkTimeMs;
            }
            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, "json format error,e:" + e.getMessage());
            return Response.error(new ParseError(response));
        }
    }

}
