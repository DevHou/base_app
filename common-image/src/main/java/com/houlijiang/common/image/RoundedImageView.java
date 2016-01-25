package com.houlijiang.common.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import com.facebook.drawee.generic.RoundingParams;

public class RoundedImageView extends CommonImageView {

    public static final String TAG = RoundedImageView.class.getSimpleName();
    public static final float DEFAULT_RADIUS = 0f;
    public static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    public RoundedImageView(Context context) {
        super(context);
        init(DEFAULT_RADIUS, DEFAULT_BORDER_COLOR, DEFAULT_BORDER_WIDTH);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0);
        try {
            float cornerRadius = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius, -1);
            int borderWidth = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_border_width, -1);
            if (cornerRadius < 0) {
                cornerRadius = DEFAULT_RADIUS;
            }
            if (borderWidth < 0) {
                borderWidth = DEFAULT_BORDER_WIDTH;
            }
            int borderColor = a.getColor(R.styleable.RoundedImageView_riv_border_color, DEFAULT_BORDER_COLOR);
            init(cornerRadius, borderColor, borderWidth);
        } catch (Exception e) {
            Log.e(TAG, "catch exception when init round image, e:" + e.getLocalizedMessage());
        } finally {
            a.recycle();
        }

    }

    private void init(float radius, int color, int width) {
        try {
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(radius);
            roundingParams.setBorder(color, width);
            getHierarchy().setRoundingParams(roundingParams);
        } catch (Exception e) {
            Log.e(TAG, "init round image catch exception, e:" + e.getLocalizedMessage());
        }
    }

}
