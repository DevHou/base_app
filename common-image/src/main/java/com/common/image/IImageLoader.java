package com.common.image;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;

import java.io.File;

/**
 * Created by houlijiang on 16/4/12.
 * 
 * 图片处理
 */
public interface IImageLoader {

    void init(Context context, File cacheDir);

    long getCacheSize();

    boolean clearCache();

    void displayImage(Context context, Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener);

    void displayImage(Fragment fragment, Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener);

    void displayImage(Activity activity, Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener);

}
