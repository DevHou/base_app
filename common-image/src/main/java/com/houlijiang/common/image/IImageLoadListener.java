package com.houlijiang.common.image;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by houlijiang on 14/11/21.
 *
 * 图片加载回调
 */
public interface IImageLoadListener {

    void onFailed(String s, View view, ImageLoadError failReason);

    void onSuccess(String s, View view, Bitmap bitmap);

}
