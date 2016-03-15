package com.common.app.base.manager;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.common.app.base.utils.AppLog;
import com.common.cache.disk.DiskCache;

import java.io.File;

/**
 * Created by houlijiang on 15/11/28.
 * 
 * 缓存的统一管理
 */
public class CacheManager {

    private static final String TAG = CacheManager.class.getSimpleName();

    private static class InstanceHolder {
        public final static CacheManager instance = new CacheManager();
    }

    public static CacheManager getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 必须程序初始化时初始化
     * 
     * @param context 上下文
     * @param cacheFile 缓存位置
     * @return 是否成功
     */
    public boolean init(Context context, File cacheFile) {
        if (cacheFile == null || !cacheFile.isDirectory()) {
            AppLog.e(TAG, "file cache dir not exist");
            return false;
        }
        if (!cacheFile.exists()) {
            if (!cacheFile.mkdirs()) {
                AppLog.e(TAG, "create file cache dir error");
                return false;
            }
        }
        return DiskCache.init(cacheFile, 1, 0);
    }

    /**
     * 查找数据
     *
     * @param key 用户使用的key
     * @return 数据或null
     */
    public @Nullable String getString(@Nullable String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        return DiskCache.getString(key);
    }

    /**
     * 查找结果
     * 
     * @param key key
     * @return 数据或者null
     */
    public @Nullable Long getLong(@Nullable String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        String value = DiskCache.getString(key);
        if (TextUtils.isEmpty(value)) {
            return null;
        } else {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                Log.e(TAG, "parse long error, e:" + e.getLocalizedMessage());
                return null;
            }
        }
    }

    /**
     * 是否包含该数据
     *
     * @param key 用户使用的key
     * @return 是否包含该数据
     */
    public boolean contains(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        return DiskCache.contains(key);
    }

    /**
     * 向cache中添加数据
     *
     * @param key 用户的key
     * @param value cache的数据
     * @return 是否成功
     */
    public boolean put(String key, String value) {
        return DiskCache.put(key, value);
    }

    /**
     * 向cache中添加数据
     *
     * @param key 用户的key
     * @param value cache的数据
     * @param timeout 超时时间 毫秒
     * @return 是否成功
     */
    public boolean put(String key, String value, long timeout) {
        return DiskCache.put(key, value, timeout);
    }

    /**
     * 向cache中添加数据
     *
     * @param key 用户的key
     * @param value cache的数据
     * @return 是否成功
     */
    public boolean put(String key, long value) {
        return DiskCache.put(key, String.valueOf(value));
    }

    /**
     * 向cache中添加数据
     *
     * @param key 用户的key
     * @param value cache的数据
     * @param timeout 超时时间 毫秒
     * @return 是否成功
     */
    public boolean put(String key, long value, long timeout) {
        return DiskCache.put(key, String.valueOf(value), timeout);
    }

    /**
     * 删除数据
     *
     * @param key 用户的key
     * @return 是否成功
     */
    public boolean remove(String key) {
        return DiskCache.delete(key);
    }

}
