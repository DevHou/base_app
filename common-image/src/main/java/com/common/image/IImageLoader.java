package com.common.image;

import android.content.Context;
import android.net.Uri;

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

    void displayImage(Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener);
}
