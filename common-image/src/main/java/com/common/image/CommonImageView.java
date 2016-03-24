package com.common.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.common.utils.AppLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by houlijiang on 15/4/2.
 * 
 * 自定义ImageView，为方便日后使用其他库
 * 注意使用时不要使用android:src 而改用app:placeHolderImage属性，同时暂时不要使用其他app属性以免以后换库后不支持
 *
 * 使用fresco，几点注意事项
 * 
 * 不要复用 DraweeHierarchies
 * 永远不要吧DraweeHierarchy 通过 DraweeView.setHierarchy 设置给不同的View。DraweeHierarchy是由一系列Drawable组成的。在Android中,
 * Drawable不能被多个View共享。
 * 
 * 不要在多个DraweeHierarchy中使用同一个Drawable
 * 原因同上。当时可以使用不同的资源ID。Android实际会创建不同的Drawable。
 */
public class CommonImageView extends SimpleDraweeView {

    private static final String TAG = CommonImageView.class.getSimpleName();
    private ControllerListener<ImageInfo> mListener;
    private IImageLoadListener mImageLoadListener;

    public CommonImageView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public CommonImageView(Context context) {
        super(context);
        init();
    }

    public CommonImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommonImageView);
            // todo fresco库以后的版本可能会直接从View继承所以，src等属性会不可用，这里为了兼容误写的src属性
            int attributeIds[] = { android.R.attr.src };
            TypedArray b = context.obtainStyledAttributes(attrs, attributeIds);
            try {
                int imageId = a.getResourceId(R.styleable.CommonImageView_imageSrc, 0);
                int imageScaleTypeInt =
                    a.getInt(R.styleable.CommonImageView_imageScaleType, ImageScaleType.centerCrop.value());
                ScalingUtils.ScaleType st = ImageScaleType.changeToFrescoType(imageScaleTypeInt);

                int resourceId = b.getResourceId(0, 0);
                if (resourceId != 0 && imageId == 0) {
                    imageId = resourceId;
                }
                GenericDraweeHierarchy h = getInnerHierarchy(context, attrs);
                if (h == null) {
                    GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
                    if (imageId != 0) {
                        h = builder.setPlaceholderImage(getResources().getDrawable(imageId), st).build();
                    }
                } else if (imageId != 0) {
                    h.setPlaceholderImage(getResources().getDrawable(imageId), st);
                }
                setHierarchy(h);
            } catch (Exception e) {
                AppLog.e(TAG, "common image get attr error, e:" + e.getLocalizedMessage());
            } finally {
                a.recycle();
                b.recycle();
            }
        }

        init();
    }

    /**
     * 子类可以重写这个方法，添加自定义参数
     */
    protected GenericDraweeHierarchy getInnerHierarchy(Context context, AttributeSet attrs) {
        return getHierarchy();
    }

    private void init() {
        mListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {
                AppLog.v(TAG, "start load image id:" + id);
            }

            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                AppLog.v(TAG, "load image id:" + id + " success");
                IImageLoadListener listener = null;
                synchronized (CommonImageView.this) {
                    if (mImageLoadListener != null) {
                        listener = mImageLoadListener;
                    }
                }
                try {
                    if (listener != null) {
                        if (imageInfo == null) {
                            listener.onSuccess(id, CommonImageView.this, 0, 0);
                        } else {
                            listener.onSuccess(id, CommonImageView.this, imageInfo.getWidth(), imageInfo.getHeight());
                        }
                    }
                } catch (Exception e) {
                    AppLog.e(TAG, "success callback e:" + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                AppLog.v(TAG, "load image id:" + id + " failed");
                IImageLoadListener listener = null;
                synchronized (CommonImageView.this) {
                    if (mImageLoadListener != null) {
                        listener = mImageLoadListener;
                    }
                }
                try {
                    if (listener != null) {
                        listener.onFailed(id, CommonImageView.this, null);
                    }
                } catch (Exception e) {
                    AppLog.e(TAG, "fail callback e:" + e.getLocalizedMessage());
                }
            }

            @Override
            public void onRelease(String id) {
                AppLog.v(TAG, "release image id:" + id);
                synchronized (CommonImageView.this) {
                    mImageLoadListener = null;
                }
            }
        };
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        // 如果设置了src，构造函数里会调用了这个，此时父类还没有init，里面viewHolder还是空就会导致空指针异常
        try {
            super.setImageDrawable(drawable);
        } catch (Exception e) {
            AppLog.e(TAG, "catch exception when setImageDrawable, e:" + e.getLocalizedMessage());
        }
    }

    @Override
    public void setImageURI(Uri uri, @Nullable Object callerContext) {
        PipelineDraweeControllerBuilder controllerBuilder =
            Fresco.newDraweeControllerBuilder().setUri(uri).setCallerContext(callerContext)
                .setOldController(getController()).setControllerListener(mListener);
        setController(controllerBuilder.build());
    }

    public void setImageURI(Uri uri, int width, int height) {
        ImageRequest request =
            ImageRequestBuilder.newBuilderWithSource(uri).setResizeOptions(new ResizeOptions(width, height)).build();
        PipelineDraweeControllerBuilder controllerBuilder =
            Fresco.newDraweeControllerBuilder().setUri(uri).setCallerContext(null).setOldController(getController())
                .setControllerListener(mListener).setImageRequest(request);
        setController(controllerBuilder.build());
    }

    public ControllerListener<ImageInfo> getListener() {
        return mListener;
    }

    public void setImageLoadListener(IImageLoadListener listener) {
        AppLog.v(TAG, "set image load listener");
        synchronized (this) {
            mImageLoadListener = listener;
        }
    }

}
