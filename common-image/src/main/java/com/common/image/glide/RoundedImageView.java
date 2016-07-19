package com.common.image.glide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;

import com.bumptech.glide.load.Transformation;
import com.common.image.ImageLoader;
import com.common.image.R;
import com.common.image.glide.transformations.RoundedCornersBorderTransformation;
import com.common.utils.AppLog;

/**
 * Created by houlijiang on 16/4/12.
 * 
 * 简单集成ImageView
 */
public class RoundedImageView extends com.common.image.CommonImageView {

    public static final String TAG = RoundedImageView.class.getSimpleName();
    public static final int DEFAULT_RADIUS = 0;
    public static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private int mCornerRadius;
    private int mBorderWidth;
    private int mBorderColor;

    public RoundedImageView(Context context) {
        this(context, null, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
        try {
            mCornerRadius = a.getDimensionPixelSize(R.styleable.RoundedImageView_rivCornerRadius, -1);
            mBorderWidth = a.getDimensionPixelSize(R.styleable.RoundedImageView_rivBorderWidth, -1);
            if (mCornerRadius < 0) {
                mCornerRadius = DEFAULT_RADIUS;
            }
            if (mBorderWidth < 0) {
                mBorderWidth = DEFAULT_BORDER_WIDTH;
            }
            mBorderColor = a.getColor(R.styleable.RoundedImageView_rivBorderColor, DEFAULT_BORDER_COLOR);
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
    @Override
    protected void loadImage(int resId) {
        ImageLoader.displayImage(getContext(), resId, this, null);
    }

    @Override
    public Transformation<Bitmap> createTransformation() {
        return new RoundedCornersBorderTransformation(getContext(), mCornerRadius, 0, mBorderWidth, mBorderColor);
    }
}
