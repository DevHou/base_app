package com.common.image;

import android.content.Context;
import android.util.AttributeSet;

import com.common.image.fresco.photodraweeview.FrescoPhotoView;

/**
 * Created by houlijiang on 16/4/13.
 * 
 * 大图查看
 */
public class BigImageView extends FrescoPhotoView {

    public BigImageView(Context context) {
        super(context);
    }

    public BigImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BigImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 子类重写
     */
    public void update(int width, int height) {
    }
}
