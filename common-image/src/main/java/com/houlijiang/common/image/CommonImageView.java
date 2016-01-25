package com.houlijiang.common.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.SimpleDraweeControllerBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.houlijiang.common.image.fresco.ConfigConstants;

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
    private ControllerListener<Object> mListener;
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
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommonImageView);
            // todo fresco库以后的版本可能会直接从View继承所以，src等属性会不可用，这里为了兼容误写的src属性
            int attributeIds[] = { android.R.attr.src };
            TypedArray b = context.obtainStyledAttributes(attrs, attributeIds);
            try {
                int imageId = a.getResourceId(R.styleable.CommonImageView_imageSrc, 0);
                int imageScaleTypeInt = a.getInt(R.styleable.CommonImageView_imageScaleType, 6);
                ScalingUtils.ScaleType st = ImageScaleType.changeToFrescoType(imageScaleTypeInt);

                int resourceId = b.getResourceId(0, 0);
                if (resourceId != 0 && imageId == 0) {
                    imageId = resourceId;
                }
                if (imageId != 0) {
                    GenericDraweeHierarchy h = getHierarchy();
                    if (h == null) {
                        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
                        h = builder.setPlaceholderImage(getResources().getDrawable(imageId), st).build();
                    } else {
                        h.setPlaceholderImage(getResources().getDrawable(imageId), st);
                    }
                    setHierarchy(h);
                }
            } catch (Exception e) {
                Log.e(TAG, "common image get attr error, e:" + e.getLocalizedMessage());
            } finally {
                a.recycle();
                b.recycle();
            }
        }

        init();
    }

    public CommonImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mListener = new BaseControllerListener<Object>() {
            @Override
            public void onSubmit(String id, Object callerContext) {
                Log.v(TAG, "start load image id:" + id);
            }

            @Override
            public void onFinalImageSet(String id, @Nullable Object imageInfo, @Nullable Animatable animatable) {
                Log.v(TAG, "load image id:" + id + " success");
                IImageLoadListener listener = null;
                synchronized (CommonImageView.this) {
                    if (mImageLoadListener != null) {
                        listener = mImageLoadListener;
                    }
                }
                if (listener != null) {
                    listener.onSuccess(id, CommonImageView.this, null);
                }
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                Log.v(TAG, "load image id:" + id + " failed");
                IImageLoadListener listener = null;
                synchronized (CommonImageView.this) {
                    if (mImageLoadListener != null) {
                        listener = mImageLoadListener;
                    }
                }
                if (listener != null) {
                    listener.onFailed(id, CommonImageView.this, null);
                }
            }

            @Override
            public void onRelease(String id) {
                Log.v(TAG, "release image id:" + id);
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
            Log.e(TAG, "catch exception when setImageDrawable, e:" + e.getLocalizedMessage());
        }
    }

    @Override
    public void setImageURI(Uri uri, @Nullable Object callerContext) {
        SimpleDraweeControllerBuilder controllerBuilder =
            ConfigConstants.getSimpleDraweeControllerBuilder(getControllerBuilder(), uri, callerContext,
                    getController());
        if (controllerBuilder instanceof AbstractDraweeControllerBuilder) {
            ((AbstractDraweeControllerBuilder<?, ?, ?, ?>) controllerBuilder).setControllerListener(mListener);
        }
        setController(controllerBuilder.build());
    }

    /**
     * 获取图片bitmap
     * 
     * @return bitmap
     */
    public Bitmap getBitmap() {
        try {
            Drawable drawable = getTopLevelDrawable();
            Bitmap bitmap;
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }
            int widthPixels = getWidth();
            int heightPixels = getHeight();
            bitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, widthPixels, heightPixels);
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "catch exception when get bitmap, e:" + e.getLocalizedMessage());
            return null;
        }
    }

    public ControllerListener<Object> getListener() {
        return mListener;
    }

    public void setImageLoadListener(IImageLoadListener listener) {
        Log.v(TAG, "set image load listener");
        synchronized (this) {
            mImageLoadListener = listener;
        }
    }

}
