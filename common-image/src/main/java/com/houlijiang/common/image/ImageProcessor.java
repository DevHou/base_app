package com.houlijiang.common.image;

import android.graphics.Bitmap;

/**
 * Created by houlijiang on 2014/9/19.
 *
 * 图片处理接口，image loader在显示之前可以自己再处理一下
 */
public interface ImageProcessor {
    Bitmap process(Bitmap bt);
}
