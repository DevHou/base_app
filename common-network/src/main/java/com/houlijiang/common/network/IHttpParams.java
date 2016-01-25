package com.houlijiang.common.network;

import java.util.Map;

/**
 * Created by houlijiang on 14/11/18.
 * 
 * http 参数接口
 */
public interface IHttpParams {

    void put(String key, String value);

    void put(String key, Object value);

    void put(String key, int value);

    void put(String key, long value);

    void remove(String key);

    Map<String, String> getParams();

}
