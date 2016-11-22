package com.common.image.glide;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.common.image.CommonImageView;
import com.common.image.IImageLoadListener;
import com.common.image.IImageLoader;
import com.common.image.ImageLoadError;
import com.common.image.ImageOptions;
import com.common.utils.AppLog;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import okhttp3.OkHttpClient;

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
    private LoggingListener<String, GlideDrawable> debugListener = new LoggingListener<>();

    @Override
    public void init(Context context, File cacheDir, boolean debug) {
        this.context = context;
        this.cacheDir = cacheDir;
        this.debug = debug;
        // glide在第一次get实例时使用GlideModule初始化，所以这里可以先设置GlideModule，再初始化glide实例
        MyGlideModule.setFileCacheDir(cacheDir.getAbsolutePath());
        Glide.get(context).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(new OkHttpClient()));
        Glide.get(context).setMemoryCategory(MemoryCategory.HIGH);
    }

    @Override
    public void onLowMemory() {
        Glide.with(context).onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW);
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
        Glide.with(context).load(url).downloadOnly(-1, -1);
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

    public void displayImage(Object c, Uri uri, CommonImageView iv, ImageOptions options, IImageLoadListener listener) {
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
                listener.onFailed("", imageView, new ImageLoadError(ImageLoadError.ERROR_NONE_URI, ""));
            }
            return;
        }
        DrawableTypeRequest request;
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
                request.error(options.getImageResOnFail());
            } else if (options.getImageOnFail() != null) {
                request.error(options.getImageOnFail());
            }

            if (options.getImageResOnLoading() != 0) {
                request.placeholder(options.getImageResOnLoading());
            } else if (options.getImageOnLoading() != null) {
                request.placeholder(options.getImageOnLoading());
            }
            if (options.getImageScaleType() != null) {
                switch (options.getImageScaleType()) {
                    case FIT_XY:
                    case FIT_START:
                    case FIT_END:
                    case CENTER:
                    case FOCUS_CROP:
                    case CENTER_CROP: {
                        request.centerCrop();
                        break;
                    }
                    case CENTER_INSIDE:
                    case FIT_CENTER: {
                        request.fitCenter();
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
                request.override(size.width, size.height);
            }
            if (options.isGif()) {
                request.asGif();
            }
            if (!TextUtils.isEmpty(options.getImageSample())) {
                DrawableRequestBuilder<String> thumbnailRequest = Glide.with(context).load(options.getImageSample());
                // 下面两个设置是避免加载的缩略图显示和实际想要的不一样，类似loading scale的问题
                if (size != null && size.width > 0 && size.height > 0) {
                    thumbnailRequest.override(size.width, size.height);
                }
                thumbnailRequest.dontAnimate();
                request.thumbnail(thumbnailRequest);
            }
            if (debug && options.isDebug()) {
                request.listener(debugListener);// debug输出
            }
        }
        // 圆形 圆角等特殊处理
        Transformation<Bitmap> bit = imageView.createTransformation();
        if (bit != null) {
            request.bitmapTransform(bit);
        }
        // 这里的dontAnimate主要是因为当placeholder和image的scaleType不同时
        // image先显示的是placeholder的scaleType，当再次滑动回来时又用的正常的scaleType显示
        // DiskCacheStrategy。ALL->DiskCacheStrategy.SOURCE 因为jpg的图片当从缓存中取时背景的白色变成了蓝色

        // request.asBitmap().encoder(new BitmapEncoder(Bitmap.CompressFormat.JPEG, 100));
        // request.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
        // 加这个可以避免绿色背景问题
        request.diskCacheStrategy(DiskCacheStrategy.SOURCE);
        request.dontAnimate().into(imageView);

    }

    private class LoggingListener<T, R> implements RequestListener<T, R> {
        @Override
        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            AppLog.d(TAG, String.format(Locale.ROOT, "onException(%s, %s, %s, %s)", e, model, target, isFirstResource),
                e);
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache,
            boolean isFirstResource) {
            AppLog.d(TAG, String.format(Locale.ROOT, "onResourceReady(%s, %s, %s, %s, %s)", resource, model, target,
                isFromMemoryCache, isFirstResource));
            return false;
        }
    }
}
