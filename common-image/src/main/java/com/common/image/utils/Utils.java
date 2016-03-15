package com.common.image.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.facebook.drawee.drawable.ScalingUtils;
import com.common.image.ImageOptions;

/**
 * Created by houlijiang on 15/9/7.
 * 
 * 图片处理用的工具类
 */
public class Utils {

    /**
     * 从资源中获取drawable
     *
     * @param id 资源ID
     * @return drawable
     */
    public static Drawable getDrawableFromResource(Context context, int id) {
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getDrawable(id);
        } else {
            drawable = context.getResources().getDrawable(id);
        }
        return drawable;
    }

    /**
     * 转换option里的scaleType到实现类的类型
     *
     * @param t option设置的类型
     */
    public static ScalingUtils.ScaleType convertScaleType(ImageOptions.ScaleType t) {
        switch (t) {
            case FIT_XY:
                return ScalingUtils.ScaleType.FIT_XY;
            case FIT_START:
                return ScalingUtils.ScaleType.FIT_START;
            case FIT_CENTER:
                return ScalingUtils.ScaleType.FIT_CENTER;
            case FIT_END:
                return ScalingUtils.ScaleType.FIT_END;
            case CENTER:
                return ScalingUtils.ScaleType.CENTER;
            case CENTER_INSIDE:
                return ScalingUtils.ScaleType.CENTER_INSIDE;
            case CENTER_CROP:
                return ScalingUtils.ScaleType.CENTER_CROP;
            case FOCUS_CROP:
            default:
                return ScalingUtils.ScaleType.CENTER_CROP;
        }
    }
}
