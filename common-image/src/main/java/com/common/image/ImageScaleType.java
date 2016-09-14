package com.common.image;

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

}
