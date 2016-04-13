package com.common.image.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.common.image.CommonImageView;
import com.common.image.IImageLoadListener;
import com.common.image.IImageLoader;
import com.common.image.ImageLoadError;
import com.common.image.ImageOptions;

import java.io.File;
import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * Created by houlijiang on 16/4/12.
 * 
 * 图片加载的glide实现
 */
public class GlideImageLoader implements IImageLoader {

    private static final String TAG = GlideImageLoader.class.getSimpleName();

    @Override
    public void init(Context context, File cacheDir) {
        Glide.get(context).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(new OkHttpClient()));
    }

    @Override
    public void displayImage(Uri uri, CommonImageView iv, ImageOptions options, IImageLoadListener listener) {
        com.common.image.glide.CommonImageView imageView = iv;
        // com.common.image.glide.CommonImageView imageView = null;
        RequestManager manager = Glide.with(imageView.getContext());
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
        DrawableTypeRequest<Uri> request = manager.load(uri);
        if (options != null) {
            if (options.getImageResForEmptyUri() != 0) {
                request.error(options.getImageResForEmptyUri());
            } else if (options.getImageForEmptyUri() != null) {
                request.error(options.getImageForEmptyUri());
            }

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
                    case CENTER_INSIDE:
                    case FOCUS_CROP:
                    case CENTER_CROP: {
                        request.centerCrop();
                        break;
                    }
                    case FIT_CENTER: {
                        request.fitCenter();
                        break;
                    }
                }
            }
            if (options.getIfGif()) {
                request.asGif();
            }
        }
        Transformation<Bitmap> bit = imageView.createTransformation();
        if (bit != null) {
            request.bitmapTransform(bit);
        }
        request.diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);

    }
}
