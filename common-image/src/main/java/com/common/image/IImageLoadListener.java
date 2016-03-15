package com.common.image;

import android.view.View;

/**
 * Created by houlijiang on 14/11/21.
 *
 * 图片加载回调
 */
public interface IImageLoadListener {

    /**
     * 图片加载失败
     * @param s 原始uri
     * @param view ImageView
     * @param failReason 失败原因F
     */
    void onFailed(String s, View view, ImageLoadError failReason);

    /**
     * 图片加载成功
     * 
     * @param s uri
     * @param view imageView
     * @param width 图片实际宽
     * @param height 图片实际高
     */
    void onSuccess(String s, View view, int width, int height);

}
