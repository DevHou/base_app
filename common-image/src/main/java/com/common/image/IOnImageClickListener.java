package com.common.image;

import android.view.View;

/**
 * Created by houlijiang on 16/4/15.
 * 
 * 图片点击回调
 */
public interface IOnImageClickListener {

    /**
     * 图片点击
     */
    void onImageClick(View view, float x, float y);

    /**
     * 图片长按
     */
    void onImageLongClick(View view);
}
