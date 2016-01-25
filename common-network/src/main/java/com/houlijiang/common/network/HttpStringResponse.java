package com.houlijiang.common.network;

/**
 * Created by houlijiang on 15/4/9.
 *
 * 如果继承自这个类则库里不进行解析，直接将结果string返回
 */
public class HttpStringResponse extends HttpResponseResult {
    public String data;
}
