package com.common.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.common.utils.AppLog;

/**
 * Created by houlijiang on 15/4/2.
 * 
 * 只是通过继承不同库的具体实现切换
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
                    ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER;
                    if (imageScaleTypeInt == ImageScaleType.center.value()) {
                        scaleType = ImageView.ScaleType.CENTER;
                    } else if (imageScaleTypeInt == ImageScaleType.centerCrop.value()) {
                        scaleType = ImageView.ScaleType.CENTER_CROP;
                    } else if (imageScaleTypeInt == ImageScaleType.centerInside.value()) {
                        scaleType = ImageView.ScaleType.CENTER_INSIDE;
                    } else if (imageScaleTypeInt == ImageScaleType.fitCenter.value()) {
                        scaleType = ImageView.ScaleType.FIT_CENTER;
                    } else if (imageScaleTypeInt == ImageScaleType.fitEnd.value()) {
                        scaleType = ImageView.ScaleType.FIT_END;
                    } else if (imageScaleTypeInt == ImageScaleType.fitStart.value()) {
                        scaleType = ImageView.ScaleType.FIT_START;
                    } else if (imageScaleTypeInt == ImageScaleType.fitXY.value()) {
                        scaleType = ImageView.ScaleType.FIT_XY;
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
    }

    public void onViewRecycled() {
        setImageResource(0);
    }

    /**
     * 加载资源图片
     *
     * @param resId 资源ID
     */
    protected void loadImage(int resId) {
        setImageResource(resId);
    }

    /**
     * 子类可以重写这个方法，返回合适的对象
     */
    public com.bumptech.glide.load.Transformation<Bitmap> createTransformation() {
        return null;
    }

    /**
     * 子类可以重写这个方法，返回合适的对象
     */
    public com.squareup.picasso.Transformation getTransform() {
        return null;
    }

    /**
     * 设置点击回调，子类应该重写
     * 
     * @param listener
     */
    public void setImageOnClickListener(IOnImageClickListener listener) {
    }

    /**
     * 更新图片大小，子类可以重写
     * 
     * @param imageInfoWidth 图片实际宽
     * @param imageInfoHeight 图片实际高
     */
    public void update(int imageInfoWidth, int imageInfoHeight) {
    }

}
