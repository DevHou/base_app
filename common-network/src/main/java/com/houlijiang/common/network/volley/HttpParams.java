package com.houlijiang.common.network.volley;

import com.houlijiang.common.network.IHttpParams;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by houlijiang on 14/11/19.
 *
 * volley 参数
 */
public class HttpParams implements IHttpParams {

    private static final String TAG = "HttpParams";

    protected final ConcurrentHashMap<String, String> urlParams = new ConcurrentHashMap<String, String>();

    public HttpParams() {
    }

    @Override
    public Map<String, String> getParams() {
        return urlParams;
    }

    @Override
    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    @Override
    public void put(String key, Object value) {
        if (key != null && value != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    @Override
    public void put(String key, int value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    @Override
    public void put(String key, long value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    @Override
    public void remove(String key) {
        if (key != null && urlParams.containsKey(key)) {
            urlParams.remove(key);
        }
    }

}
