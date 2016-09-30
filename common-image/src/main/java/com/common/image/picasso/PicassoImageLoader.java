package com.common.image.picasso;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.common.image.CommonImageView;
import com.common.image.IImageLoadListener;
import com.common.image.IImageLoader;
import com.common.image.ImageLoadError;
import com.common.image.ImageOptions;
import com.common.utils.AppLog;
import com.common.utils.FileUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;

/**
 * Created by houlijiang on 16/8/26.
 * 
 * 图片加载的picasso实现
 * 
 * 目前这个库的问题：
 * 
 */
public class PicassoImageLoader implements IImageLoader {

    private static final String TAG = PicassoImageLoader.class.getSimpleName();

    private static final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();// 分配的可用内存
    private static final int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / 4;// 使用的缓存数量

    private Context context;
    private File cacheDir;

    LruCache mMemoryCache;
    private OkHttp3Downloader mDownloader;

    @Override
    public void init(Context context, File cacheDir, boolean debug) {
        this.context = context;
        this.cacheDir = cacheDir;

        try {
            mDownloader = new OkHttp3Downloader(cacheDir, Integer.MAX_VALUE);
            mMemoryCache = new LruCache(MAX_MEMORY_CACHE_SIZE);
            Picasso picasso = new Picasso.Builder(context).memoryCache(mMemoryCache).downloader(mDownloader).build();
            // if (debug) {
            // picasso.setIndicatorsEnabled(true); // For debugging
            // }
            Picasso.setSingletonInstance(picasso);
            AppLog.d(TAG, "init picasso memory size:" + MAX_MEMORY_CACHE_SIZE);
        } catch (Exception e) {
            AppLog.e(TAG, "init picasso e:" + e.getLocalizedMessage());
        }
    }

    @Override
    public void onLowMemory() {
        mMemoryCache.clear();
    }

    @Override
    public long getCacheSize() {
        if (!cacheDir.exists()) {
            return 0;
        } else {
            return getFileSize(cacheDir);
        }
    }

    private long getFileSize(File file) {
        long size = 0;
        try {
            if (file.isDirectory()) {
                if (file.listFiles() != null) {
                    for (File f : file.listFiles()) {
                        size += getFileSize(f);
                    }
                } else {
                    return 0;
                }
            } else {
                size = file.length();
            }
        } catch (Exception e) {
            Log.e(TAG, "get file size e:" + e.getLocalizedMessage());
        }
        return size;
    }

    @Override
    public boolean clearCache() {
        try {
            FileUtils.deleteDirectory(cacheDir);
            return true;
        } catch (Exception e) {
            AppLog.e(TAG, "clear disk cache e:" + e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void displayImage(Context context, Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener) {
        displayImage((Object) context, uri, imageView, options, listener);
    }

    @Override
    public void displayImage(Fragment fragment, Uri uri, final CommonImageView imageView, final ImageOptions options,
                             final IImageLoadListener listener) {
        displayImage((Object) fragment, uri, imageView, options, listener);
    }

    @Override
    public void displayImage(Activity activity, Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener) {
        displayImage((Object) activity, uri, imageView, options, listener);
    }

    public void displayImage(final Object c, final Uri uri, CommonImageView iv, final ImageOptions options,
        IImageLoadListener listener) {
        final CommonImageView imageView = iv;

        if (uri == null) {
            // 显示空的图片的
            if (options != null) {
                if (options.getImageForEmptyUri() != null) {
                    imageView.setImageDrawable(options.getImageForEmptyUri());
                } else if (options.getImageResForEmptyUri() != 0) {
                    Picasso.with(context).load(options.getImageResForEmptyUri()).into(imageView);
                }
            }
            if (listener != null) {
                listener.onFailed("", imageView, new ImageLoadError(ImageLoadError.ERROR_NONE_URI, ""));
            }
            return;
        }
        if (options != null && !TextUtils.isEmpty(options.getImageSample())) {
            Uri sample = null;
            try {
                sample = Uri.parse(options.getImageSample());
            } catch (Exception e) {
                AppLog.e(TAG, "parse sample to uri e:" + e.getLocalizedMessage());
            }
            if (sample != null) {
                RequestCreator request2 = createRequest(c, sample, imageView, options, true);
                request2.into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        AppLog.d(TAG, "load sample finish, will load ori image ");
                        RequestCreator request = createRequest(c, uri, imageView, options, false);
                        request.into(imageView);
                    }

                    @Override
                    public void onError() {

                    }
                });
                AppLog.d(TAG, "load sample " + sample);
            } else {
                AppLog.d(TAG, "create sample error, load ori image " + uri);
                RequestCreator request = createRequest(c, uri, imageView, options, true);
                request.into(imageView);
            }
        } else {
            AppLog.d(TAG, "load ori image " + uri);
            RequestCreator request = createRequest(c, uri, imageView, options, true);
            request.into(imageView);
        }

    }

    private RequestCreator createRequest(Object c, Uri uri, CommonImageView iv,
        ImageOptions options, boolean showHolder) {
        Picasso picasso = Picasso.with(context);
        RequestCreator request;
        if ("res".equals(uri.getScheme())) {
            int resId = 0;
            try {
                resId = Integer.parseInt(uri.getLastPathSegment());
            } catch (Exception e) {
                AppLog.e(TAG, "parse res error:" + e.getLocalizedMessage());
            }
            request = picasso.load(resId);
        } else {
            request = picasso.load(uri);
        }
        if (options != null) {
            if (options.getImageResOnFail() != 0) {
                request.error(options.getImageResOnFail());
            } else if (options.getImageOnFail() != null) {
                request.error(options.getImageOnFail());
            }
            if (showHolder) {
                if (options.getImageResOnLoading() != 0) {
                    request.placeholder(options.getImageResOnLoading());
                } else if (options.getImageOnLoading() != null) {
                    request.placeholder(options.getImageOnLoading());
                }
            } else {
                request.noPlaceholder();
            }
            if (options.getImageScaleType() != null) {
                switch (options.getImageScaleType()) {
                    case FIT_XY:
                    case FIT_START:
                    case FIT_END:
                    case CENTER:
                    case FIT_CENTER:
                    case FOCUS_CROP:
                    case CENTER_CROP: {
                        request.centerCrop();
                        break;
                    }
                    case CENTER_INSIDE: {
                        request.centerInside();
                        break;
                    }
                }
            }
            ImageOptions.ImageSize size = options.getImageSize();
            if (size != null && size.width > 0 && size.height > 0) {
                if (iv.getLayoutParams() != null) {
                    iv.getLayoutParams().width = size.width;
                    iv.getLayoutParams().height = size.height;
                }
                request.resize(size.width, size.height).onlyScaleDown();
            } else {
                request.fit();
            }
        }
        // 圆形 圆角等特殊处理
        if (iv.getTransform() != null) {
            request.transform(iv.getTransform());
        }
        return request;
    }

}
