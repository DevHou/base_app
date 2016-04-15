package com.common.image;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by houlijiang on 15/4/2.
 * 
 * 只是通过继承不同库的具体实现切换
 */
public class CommonImageView extends com.common.image.glide.CommonImageView {

    public CommonImageView(Context context) {
        super(context);
    }

    public CommonImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommonImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageOnClickListener(IOnImageClickListener listener) {

    }
}
