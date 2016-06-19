package com.common.image.fresco;

import com.common.image.ImageOptions;
import com.facebook.drawee.drawable.ScalingUtils;

/**
 * Created by houlijiang on 16/6/14.
 * 
 * 工具类
 */
public class FrescoUtils {

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
