package com.common.image.picasso;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.common.image.ImageScaleType;
import com.common.image.R;
import com.common.utils.AppLog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Created by houlijiang on 16/8/26.
 *
 * 简单继承ImageView
 */
public class CommonImageView extends ImageView {

    private static final String TAG = CommonImageView.class.getSimpleName();

    public CommonImageView(Context context) {
        this(context, null, 0);
    }

    public CommonImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommonImageView);
            int attributeIds[] = { android.R.attr.src };
            TypedArray b = context.obtainStyledAttributes(attrs, attributeIds);
            try {
                int imageId = a.getResourceId(R.styleable.CommonImageView_imageSrc, 0);
                int imageScaleTypeInt = a.getInt(R.styleable.CommonImageView_imageScaleType, -1);

                if (imageScaleTypeInt != -1) {
                    ScaleType scaleType = ScaleType.CENTER;
                    if (imageScaleTypeInt == ImageScaleType.center.value()) {
                        scaleType = ScaleType.CENTER;
                    } else if (imageScaleTypeInt == ImageScaleType.centerCrop.value()) {
                        scaleType = ScaleType.CENTER_CROP;
                    } else if (imageScaleTypeInt == ImageScaleType.centerInside.value()) {
                        scaleType = ScaleType.CENTER_INSIDE;
                    } else if (imageScaleTypeInt == ImageScaleType.fitCenter.value()) {
                        scaleType = ScaleType.FIT_CENTER;
                    } else if (imageScaleTypeInt == ImageScaleType.fitEnd.value()) {
                        scaleType = ScaleType.FIT_END;
                    } else if (imageScaleTypeInt == ImageScaleType.fitStart.value()) {
                        scaleType = ScaleType.FIT_START;
                    } else if (imageScaleTypeInt == ImageScaleType.fitXY.value()) {
                        scaleType = ScaleType.FIT_XY;
                    }
                    setScaleType(scaleType);
                }

                int resourceId = b.getResourceId(0, 0);
                if (resourceId != 0 && imageId == 0) {
                    imageId = resourceId;
                }

                loadImage(imageId);
            } catch (Exception e) {
                AppLog.e(TAG, "common image get attr error, e:" + e.getLocalizedMessage());
            } finally {
                a.recycle();
                b.recycle();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 取消请求
        Picasso.with(getContext()).cancelRequest(this);
    }

    /**
     * 加载资源图片
     *
     * @param resId 资源ID
     */
    protected void loadImage(int resId) {
        if (resId != 0) {
            setImageResource(resId);
        }
    }

    public Transformation getTransform() {
        return null;
    }
}
