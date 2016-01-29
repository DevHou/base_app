package com.houlijiang.common.network.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.houlijiang.common.cache.memory.LruBitmapCache;
import com.jjc.volley.toolbox.ImageLoader;


/**
 * Created by houlijiang on 14/11/24.
 *
 * 图片缓存，默认是5个屏大小的图片
 */
public class ImageCache extends LruBitmapCache implements ImageLoader.ImageCache {

    private static ImageCache mCache;
    private static final int MAX_IMAGE_CACHE_OF_SCREEN = 5;

    public static synchronized ImageCache getInstance(Context context) {
        if (mCache == null) {
            int maxSize = getCacheSize(context);
            mCache = new ImageCache(maxSize);
        }
        return mCache;
    }

    private ImageCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public Bitmap getBitmap(String url) {
        return super.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        super.put(url, bitmap);
    }

    public static int getCacheSize(Context context) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final int screenWidth = displayMetrics.widthPixels;
        final int screenHeight = displayMetrics.heightPixels;
        final int screenBytes = screenWidth * screenHeight * 4; // 4 bytes per pixel
        return screenBytes * MAX_IMAGE_CACHE_OF_SCREEN;
    }
}
