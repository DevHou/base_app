package com.common.image;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 带圆角的图
 * 
 * 只是通过继承不同库的具体实现切换
 */
public class RoundedImageView extends com.common.image.glide.RoundedImageView {

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
