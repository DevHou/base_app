package com.houlijiang.common.image;

import com.facebook.drawee.drawable.ScalingUtils;

/**
 * Created by houlijiang on 15/9/7.
 *
 * 支持的所有缩放模式
 */
public enum ImageScaleType {

    fitXY(0), fitStart(1), fitCenter(2), fitEnd(3), center(4), centerInside(5), centerCrop(6);

    private int value = 0;

    ImageScaleType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static ImageScaleType valueOf(int value) {
        switch (value) {
            case 0:
                return fitXY;
            case 1:
                return fitStart;
            case 2:
                return fitCenter;
            case 3:
                return fitEnd;
            case 4:
                return center;
            case 5:
                return centerInside;
            case 6:
                return centerCrop;
        }
        return centerCrop;
    }

    /**
     * 对应fresco里的类型字符串
     */
    public static ScalingUtils.ScaleType changeToFrescoType(int value) {
        switch (value) {
            case 0:
                return ScalingUtils.ScaleType.FIT_XY;
            case 1:
                return ScalingUtils.ScaleType.FIT_START;
            case 2:
                return ScalingUtils.ScaleType.FIT_CENTER;
            case 3:
                return ScalingUtils.ScaleType.FIT_END;
            case 4:
                return ScalingUtils.ScaleType.CENTER;
            case 5:
                return ScalingUtils.ScaleType.CENTER_INSIDE;
            case 6:
                return ScalingUtils.ScaleType.CENTER_CROP;
        }
        return ScalingUtils.ScaleType.CENTER_CROP;
    }
}
