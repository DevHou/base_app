package com.common.image.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.target.ViewTarget;
import com.common.image.R;
import com.common.utils.AppLog;

/**
 * Created by houlijiang on 16/7/14.
 * 
 * 给glide设置setTag的id，避免外部使用时不能setTag
 */
public class MyGlideModule implements GlideModule {

    private static final String TAG = MyGlideModule.class.getSimpleName();

    private static String FILE_CACHE_DIR = "";

    public static String getFileCacheDir() {
        return FILE_CACHE_DIR;
    }

    public static void setFileCacheDir(String dir) {
        FILE_CACHE_DIR = dir;
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        ViewTarget.setTagId(R.id.glide_image_tag_id);
        builder.setDiskCache(new DiskLruCacheFactory(FILE_CACHE_DIR, "glide", 500 * 1024 * 1024));

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        builder.setMemoryCache(new LruResourceCache(defaultMemoryCacheSize / 3));
        builder.setBitmapPool(new LruBitmapPool(defaultBitmapPoolSize / 3));
        AppLog.d(TAG, "cache size:" + defaultMemoryCacheSize + " pool:" + defaultBitmapPoolSize);
        AppLog.d(TAG, "set cache size:" + defaultMemoryCacheSize / 3 + " pool:" + defaultBitmapPoolSize / 3);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
