package com.common.image;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by houlijiang on 15/4/2.
 * 
 * 只是通过继承不同库的具体实现切换
 */
public class CommonImageView extends com.common.image.fresco.CommonImageView {

    public CommonImageView(Context context) {
        super(context);
    }

    public CommonImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommonImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 设置点击回调，子类应该重写
     * 
     * @param listener 回调
     */
    public void setImageOnClickListener(IOnImageClickListener listener) {

    }

    /**
     * 更新图片大小，子类可以重写
     * 
     * @param imageInfoWidth 图片实际宽
     * @param imageInfoHeight 图片实际高
     */
    public void update(int imageInfoWidth, int imageInfoHeight) {
    }
}
