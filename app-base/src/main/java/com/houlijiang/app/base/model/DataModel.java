package com.houlijiang.app.base.model;

import com.google.gson.JsonSyntaxException;
import com.houlijiang.common.utils.JsonUtils;

import java.io.Serializable;

/**
 * Created by houlijiang on 11/5/15.
 *
 * 业务层的model，所有服务器返回的model都应该继承自这个model
 * 这里的model和服务器是对应的
 *
 * 本类对应的是结果集中的 data 字段
 *
 */
public abstract class DataModel implements Serializable {

    private static final String TAG = DataModel.class.getSimpleName();

    /**
     * 解析json
     *
     * @param json 原始数据结构
     * @param classOfT 需要转成的class
     * @return 转好的类实例
     */
    public static <T extends DataModel> T doParse(String json, final Class<T> classOfT) {

        T data;
        try {
            data = JsonUtils.parseString(json, classOfT);
        } catch (JsonSyntaxException e) {
            data = null;
        }
        return data;
    }

}
