package com.common.image;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;

import java.io.File;

/**
 * Created by houlijiang on 16/4/12.
 * 
 * 图片处理
 */
public interface IImageLoader {

    void init(Context context, File cacheDir, boolean debug);

    /**
     * 一定要在主线程
     */
    void onLowMemory();

    /**
     * 一定要在后台线程
     * 
     * @return 字节数
     */
    long getCacheSize();

    /**
     * 一定要在后台线程
     *
     * @return 是否成功
     */
    boolean clearCache();

    /**
     * 预取并缓存图片
     * 
     * @param url 图片地址
     */
    void cacheImage(Context context, String url);

    void displayImage(Context context, Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener);

    void displayImage(Fragment fragment, Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener);

    void displayImage(Activity activity, Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener);

}
