package com.houlijiang.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.houlijiang.common.image.config.ConfigConstants;
import com.houlijiang.common.image.utils.Utils;

import java.io.File;

/**
 * Created by houlijiang on 14/11/21.
 * 
 * 图片加载工具类
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    // url后处理回调
    private static IUrlProcessor mProcessor;

    /**
     * 初始化
     *
     * @param context 上下文
     * @param cacheDir 缓存存储的目录
     */
    public static void init(Context context, File cacheDir) {
        init(context, cacheDir, null);
    }

    /**
     * 初始化
     * 
     * @param context 上下文
     * @param cacheDir 缓存存储的目录
     */
    public static void init(Context context, File cacheDir, IUrlProcessor filter) {
        Fresco.initialize(context, ConfigConstants.getImagePipelineConfig(context, cacheDir));// 图片缓存初始化配置
        if (filter != null) {
            mProcessor = filter;
        }
    }

    /**
     * 显示图片
     *
     * @param file 图片文件
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(File file, CommonImageView imageView, ImageOptions options) {
        displayImage(Uri.fromFile(file), imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param file 图片文件
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(File file, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        displayImage(Uri.fromFile(file), imageView, options, listener);
    }

    /**
     * 显示图片
     *
     * @param resId 图片drawable的id
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(int resId, CommonImageView imageView, ImageOptions options) {
        Uri uri = Uri.parse("res:///" + String.valueOf(resId));
        displayImage(uri, imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param resId 图片drawable的id
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(int resId, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = Uri.parse("res:///" + String.valueOf(resId));
        displayImage(uri, imageView, options, listener);
    }

    /**
     * 显示图片
     *
     * @param url 图片url
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(String url, CommonImageView imageView, ImageOptions options) {
        displayImage(url, imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param url 图片url
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     * @param listener 回调
     */
    public static void displayImage(String url, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = null;
        if (!TextUtils.isEmpty(url)) {
            if (mProcessor != null) {
                url = mProcessor.filter(imageView, url);
            }
            uri = Uri.parse(url);
        }
        displayImage(uri, imageView, options, listener);
    }

    /**
     * 显示图片
     * 
     * Scheme Fetch method used
     * File on network http://, https:// HttpURLConnection or network layer
     * File on device file:// FileInputStream
     * Content provider content:// ContentResolver
     * Asset in app asset:// AssetManager
     * Resource in app res:// as in res://12345 Resources.openRawResource
     * Data in URI data:mime/type;base64, Following data URI spec (UTF-8 only)
     *
     * @param uri 图片uri，可以是file drawable assets url
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     * @param listener 回调
     */
    public static void displayImage(Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener) {
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
                    Utils.convertScaleType(options.getLoadingScaleType()));
            } else if (options.getImageResOnLoading() != 0) {
                builder.setPlaceholderImage(
                    Utils.getDrawableFromResource(imageView.getContext(), options.getImageResOnLoading()),
                    Utils.convertScaleType(options.getLoadingScaleType()));
            }
            if (options.getImageOnFail() != null) {
                builder.setFailureImage(options.getImageOnFail(), Utils.convertScaleType(options.getFailScaleType()));
            } else if (options.getImageResOnFail() != 0) {
                builder.setFailureImage(
                    Utils.getDrawableFromResource(imageView.getContext(), options.getImageResOnFail()),
                    Utils.convertScaleType(options.getFailScaleType()));
            }
            // 设置scaleType
            builder.setActualImageScaleType(Utils.convertScaleType(options.getImageScaleType()));
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
                                Log.e(TAG, "post process image error, e:" + e.getLocalizedMessage());
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

    /**
     * 对url做处理
     */
    public interface IUrlProcessor {
        String filter(CommonImageView civ, String url);
    }
}
