package com.houlijiang.common.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.houlijiang.common.image.zoomableImage.zoomable.ZoomableDraweeView;

/**
 * Created by houlijiang on 15/4/2.
 * 
 * 继承自自定义的可以缩放的view
 * 
 * 部分代码和CommonImageView重复，日后再优化
 */
public class ZoomableImageView extends ZoomableDraweeView {

    private static final String TAG = ZoomableImageView.class.getSimpleName();

    public ZoomableImageView(Context context) {
        super(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
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
                    GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
                    GenericDraweeHierarchy h =
                        builder.setPlaceholderImage(getResources().getDrawable(imageId), st).build();
                    setHierarchy(h);
                }
            } catch (Exception e) {
                Log.e(TAG, "common image get attr error, e:" + e.getLocalizedMessage());
            } finally {
                a.recycle();
                b.recycle();
            }
        }

    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

}
