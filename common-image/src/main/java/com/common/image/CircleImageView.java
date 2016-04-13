package com.common.image;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 圆形图片
 * 
 * 只是通过继承不同库的具体实现切换
 */
public class CircleImageView extends com.common.image.glide.CircleImageView {

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
