package com.houlijiang.common.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import com.facebook.drawee.generic.RoundingParams;

/**
 * 圆形图片
 */
public class CircleImageView extends CommonImageView {

    private static final String TAG = CircleImageView.class.getSimpleName();
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    public CircleImageView(Context context) {
        super(context);
        init(DEFAULT_BORDER_WIDTH, DEFAULT_BORDER_COLOR);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);
        try {
            int width = a.getDimensionPixelSize(R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
            int color = a.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR);
            init(width, color);
        } catch (Exception e) {
            Log.e(TAG, "circle image get attr error, e:" + e.getLocalizedMessage());
        } finally {
            a.recycle();
        }
    }

    private void init(int width, int color) {
        try {
            RoundingParams roundingParams = RoundingParams.asCircle();
            roundingParams.setBorder(color, width);
            getHierarchy().setRoundingParams(roundingParams);
        } catch (Exception e) {
            Log.e(TAG, "init circle image, e:" + e.getLocalizedMessage());
        }
    }
}
