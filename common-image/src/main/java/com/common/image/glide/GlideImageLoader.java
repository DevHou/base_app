package com.common.image.glide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.common.image.CommonImageView;
import com.common.image.IImageLoadListener;
import com.common.image.IImageLoader;
import com.common.image.ImageLoadError;
import com.common.image.ImageOptions;
import com.common.utils.AppLog;

import java.io.File;
import java.util.Locale;

/**
 * Created by houlijiang on 16/4/12.
 * <p/>
 * 图片加载的glide实现
 * <p/>
 * 目前这个库的问题：
 * PlaceHolder 被拉伸了，这个暂时可以通过在XML中设置 scaleType=center来解决
 */
public class GlideImageLoader implements IImageLoader {

    private static final String TAG = GlideImageLoader.class.getSimpleName();

    private Context context;
    private File cacheDir;
    private boolean debug;
    private LoggingListener<Drawable> debugListener = new LoggingListener<>();

    @Override
    public void init(Context context, File cacheDir, boolean debug) {
        this.context = context;
        this.cacheDir = cacheDir;
        this.debug = debug;
        // glide在第一次get实例时使用GlideModule初始化，所以这里可以先设置GlideModule，再初始化glide实例
        MyGlideModule.setFileCacheDir(cacheDir.getAbsolutePath());
        Glide.get(context).setMemoryCategory(MemoryCategory.HIGH);
    }

    @Override
    public void onLowMemory() {
        // Glide.with(context).onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW);
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
            Glide.get(null).clearDiskCache();
            return true;
        } catch (Exception e) {
            AppLog.e(TAG, "clear disk cache e:" + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * 预取并缓存图片
     *
     * @param url 图片地址
     */
    @Override
    public void cacheImage(Context context, String url) {
        Glide.with(context).load(url).submit(-1, -1);
    }

    @Override
    public void displayImage(Context context, Uri uri, final CommonImageView imageView,
            final ImageOptions options,
            final IImageLoadListener listener) {
        displayImage((Object) context, uri, imageView, options, listener);
    }

    @Override
    public void displayImage(Fragment fragment, Uri uri, final CommonImageView imageView,
            final ImageOptions options,
            final IImageLoadListener listener) {
        displayImage((Object) fragment, uri, imageView, options, listener);
    }

    @Override
    public void displayImage(Activity activity, Uri uri, final CommonImageView imageView,
            final ImageOptions options,
            final IImageLoadListener listener) {
        displayImage((Object) activity, uri, imageView, options, listener);
    }

    @SuppressLint("CheckResult")
    public void displayImage(Object c, Uri uri, CommonImageView iv, ImageOptions options,
            IImageLoadListener listener) {
        final CommonImageView imageView = iv;

        RequestManager manager;
        if (c instanceof Fragment) {
            manager = Glide.with((Fragment) c);
        } else if (c instanceof Activity) {
            manager = Glide.with((Activity) c);
        } else if (c instanceof Context) {
            manager = Glide.with((Context) c);
        } else {
            manager = Glide.with(imageView.getContext());
        }
        if (uri == null) {
            // 显示空的图片的
            if (options != null) {
                if (options.getImageForEmptyUri() != null) {
                    manager.load(options.getImageForEmptyUri()).into(imageView);
                } else if (options.getImageResForEmptyUri() != 0) {
                    manager.load(options.getImageResForEmptyUri()).into(imageView);
                }
            }
            if (listener != null) {
                listener.onFailed("", imageView,
                        new ImageLoadError(ImageLoadError.ERROR_NONE_URI, ""));
            }
            return;
        }
        RequestBuilder request;
        RequestOptions requestOptions = new RequestOptions();
        if (options != null && options.isGif()) {
            manager.asGif();
        }
        // 特殊处理资源加载
        if ("res".equals(uri.getScheme())) {
            int resId = 0;
            try {
                resId = Integer.parseInt(uri.getLastPathSegment());
            } catch (Exception e) {
                AppLog.e(TAG, "parse res error:" + e.getLocalizedMessage());
            }
            request = manager.load(resId);
        } else {
            request = manager.load(uri);
        }
        if (options != null) {
            if (options.getImageResOnFail() != 0) {
                requestOptions.error(options.getImageResOnFail());
            } else if (options.getImageOnFail() != null) {
                requestOptions.error(options.getImageOnFail());
            }

            if (options.getImageResOnLoading() != 0) {
                requestOptions.placeholder(options.getImageResOnLoading());
            } else if (options.getImageOnLoading() != null) {
                requestOptions.placeholder(options.getImageOnLoading());
            }
            if (options.getImageScaleType() != null) {
                switch (options.getImageScaleType()) {
                    case FIT_XY:
                    case FIT_START:
                    case FIT_END:
                    case CENTER:
                    case FOCUS_CROP:
                    case CENTER_CROP: {
                        requestOptions.centerCrop();
                        break;
                    }
                    case CENTER_INSIDE:
                    case FIT_CENTER: {
                        requestOptions.fitCenter();
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
                requestOptions.override(size.width, size.height);
            }
            if (!TextUtils.isEmpty(options.getImageSample())) {
                RequestBuilder<Drawable> thumbnailRequest = Glide.with(context).load(
                        options.getImageSample());
                RequestOptions sampleRequestOptions = new RequestOptions();
                // 下面两个设置是避免加载的缩略图显示和实际想要的不一样，类似loading scale的问题
                if (size != null && size.width > 0 && size.height > 0) {
                    sampleRequestOptions.override(size.width, size.height);
                }
                sampleRequestOptions.dontAnimate();
                request.apply(sampleRequestOptions).thumbnail(thumbnailRequest);
            }
            if (debug && options.isDebug()) {
                request.listener(debugListener);// debug输出
            }
        }

        // 圆形 圆角等特殊处理
        Transformation<Bitmap> bit = imageView.createTransformation();
        if (bit != null) {
            requestOptions.transform(bit);
        }

        // 这里的dontAnimate主要是因为当placeholder和image的scaleType不同时
        // image先显示的是placeholder的scaleType，当再次滑动回来时又用的正常的scaleType显示
        requestOptions.dontAnimate();
        request.apply(requestOptions).into(imageView);

    }

    private class LoggingListener<R> implements RequestListener<R> {

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<R> target,
                boolean isFirstResource) {
            AppLog.d(TAG, String.format(Locale.ROOT, "onException(%s, %s, %s, %s)",
                    e, model, target, isFirstResource), e);
            return false;
        }

        @Override
        public boolean onResourceReady(R resource, Object model, Target<R> target,
                DataSource dataSource, boolean isFirstResource) {
            AppLog.d(TAG, String.format(Locale.ROOT, "onResourceReady(%s, %s, %s, %s)", resource,
                    model, target, isFirstResource));
            return false;
        }
    }
}
