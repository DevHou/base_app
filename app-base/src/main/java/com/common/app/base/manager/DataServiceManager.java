package com.common.app.base.manager;

import android.text.TextUtils;

import com.common.app.base.service.BaseDataService;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by houlijiang on 15/11/30.
 *
 * 数据层manager
 * 管理service，service必须主动注册
 */
public class DataServiceManager {

    // service的map
    private static final ConcurrentHashMap<String, BaseDataService> map = new ConcurrentHashMap<>();

    /**
     * 注册service
     * 
     * @param name 名字
     * @param service 对应service
     * @return 是否注册成功
     */
    public static boolean registerService(String name, BaseDataService service) {
        if (TextUtils.isEmpty(name) || map.contains(name)) {
            return false;
        }
        map.put(name, service);
        return true;
    }

    /**
     * 注销service
     * 
     * @param name service对应key
     * @return 是否成功
     */
    public static boolean unRegisterService(String name) {
        if (TextUtils.isEmpty(name) || !map.contains(name)) {
            return false;
        }
        map.remove(name);
        return true;
    }

    /**
     * 获取service
     * 
     * @param name service的key
     * @return 对应service或者null
     */
    public static BaseDataService getService(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        return map.get(name);
    }
}
