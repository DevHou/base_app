package com.common.image.fresco;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.common.image.CommonImageView;
import com.common.image.IImageLoadListener;
import com.common.image.IImageLoader;
import com.common.image.ImageLoadError;
import com.common.image.ImageOptions;
import com.common.image.fresco.config.ConfigConstants;
import com.common.image.utils.Utils;
import com.common.utils.AppLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

/**
 * Created by houlijiang on 16/4/12.
 * 
 * Fresco相关操作
 */
public class FrescoImageLoader implements IImageLoader {

    private static final String TAG = FrescoImageLoader.class.getSimpleName();
    private File cacheDir;

    @Override
    public void init(Context context, File cacheDir) {
        this.cacheDir = cacheDir;
        Fresco.initialize(context, ConfigConstants.getImagePipelineConfig(context, cacheDir));// 图片缓存初始化配置
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
        Fresco.getImagePipeline().clearCaches();
        return true;
    }

    @Override
    public void displayImage(Uri uri, CommonImageView iv, final ImageOptions options, IImageLoadListener listener) {
        com.common.image.fresco.CommonImageView imageView = iv;
        // com.common.image.fresco.CommonImageView imageView = null;
        if (uri == null) {
            // 显示空的图片的
            if (options != null) {
                if (options.getImageForEmptyUri() != null) {
                    if (imageView.getHierarchy() != null) {
                        imageView.getHierarchy().setPlaceholderImage(options.getImageForEmptyUri());
                    } else {
                        imageView.setImageDrawable(options.getImageForEmptyUri());
                    }
                } else if (options.getImageResForEmptyUri() != 0) {
                    imageView.setImageURI(Uri.parse("res:///" + String.valueOf(options.getImageResForEmptyUri())));
                }
            }
            if (listener != null) {
                listener.onFailed("", imageView, new ImageLoadError(ImageLoadError.ERROR_NONE_URI, ""));
            }
            return;
        }
        // 根据option设置显示
        if (options != null) {
            // 配置失败、加载中的显示图片
            GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(imageView.getContext().getResources());
            if (options.getImageOnLoading() != null) {
                builder.setPlaceholderImage(options.getImageOnLoading(),
                    FrescoUtils.convertScaleType(options.getLoadingScaleType()));
            } else if (options.getImageResOnLoading() != 0) {
                builder.setPlaceholderImage(
                    Utils.getDrawableFromResource(imageView.getContext(), options.getImageResOnLoading()),
                    FrescoUtils.convertScaleType(options.getLoadingScaleType()));
            }
            if (options.getImageOnFail() != null) {
                builder.setFailureImage(options.getImageOnFail(),
                    FrescoUtils.convertScaleType(options.getFailScaleType()));
            } else if (options.getImageResOnFail() != 0) {
                builder.setFailureImage(
                    Utils.getDrawableFromResource(imageView.getContext(), options.getImageResOnFail()),
                    FrescoUtils.convertScaleType(options.getFailScaleType()));
            }
            // 设置scaleType
            builder.setActualImageScaleType(FrescoUtils.convertScaleType(options.getImageScaleType()));
            // 圆角参数重新设置回去
            if (imageView.hasHierarchy()) {
                builder.setRoundingParams(imageView.getHierarchy().getRoundingParams());
            }
            imageView.setHierarchy(builder.build());
            // 设置后处理回调
            if (options.getProcessor() != null) {
                ImageRequest request =
                    ImageRequestBuilder.newBuilderWithSource(uri).setPostprocessor(new BasePostprocessor() {
                        @Override
                        public void process(Bitmap bitmap) {
                            try {
                                options.getProcessor().process(bitmap);
                            } catch (Exception e) {
                                AppLog.e(TAG, "post process image error, e:" + e.getLocalizedMessage());
                            }
                        }
                    }).build();

                PipelineDraweeController controller =
                    (PipelineDraweeController) Fresco.newDraweeControllerBuilder().setImageRequest(request)
                        .setOldController(imageView.getController()).build();
                imageView.setController(controller);
            }
        }
        // 设置图片加载回调
        if (listener != null) {
            imageView.setImageLoadListener(listener);
        }

        if (options != null && options.getImageSize() != null && options.getImageSize().width > 0
            && options.getImageSize().height > 0) {
            imageView.setImageURI(uri, options.getImageSize().width, options.getImageSize().height);
        } else {
            imageView.setImageURI(uri);
        }
    }
}
