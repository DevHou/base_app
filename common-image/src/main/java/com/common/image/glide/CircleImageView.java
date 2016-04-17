package com.common.image.glide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;

import com.bumptech.glide.load.Transformation;
import com.common.image.ImageLoader;
import com.common.image.R;
import com.common.image.glide.transformations.CropCircleBorderTransformation;
import com.common.utils.AppLog;

/**
 * Created by houlijiang on 16/4/12.
 * 
 * 简单集成ImageView
 */
public class CircleImageView extends com.common.image.CommonImageView {

    private static final String TAG = CircleImageView.class.getSimpleName();
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private int mBorderWidth;
    private int mBorderColor;

    public CircleImageView(Context context) {
        this(context, null, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView);
        try {
            mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_civBorderWidth, DEFAULT_BORDER_WIDTH);
            mBorderColor = a.getColor(R.styleable.CircleImageView_civBorderColor, DEFAULT_BORDER_COLOR);
        } catch (Exception e) {
            AppLog.e(TAG, "circle image get attr error, e:" + e.getLocalizedMessage());
        } finally {
            a.recycle();
        }
    }

    /**
     * 加载资源图片
     *
     * @param resId 资源ID
     */
    protected void loadImage(int resId) {
        ImageLoader.displayImage(resId, this, null);
    }

    @Override
    public Transformation<Bitmap> createTransformation() {
        return new CropCircleBorderTransformation(getContext(), mBorderWidth, mBorderColor);
    }
}
