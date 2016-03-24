package com.common.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.common.utils.AppLog;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;

public class RoundedImageView extends CommonImageView {

    public static final String TAG = RoundedImageView.class.getSimpleName();
    public static final float DEFAULT_RADIUS = 0f;
    public static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected GenericDraweeHierarchy getInnerHierarchy(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
        try {
            float cornerRadius = a.getDimensionPixelSize(R.styleable.RoundedImageView_rivCornerRadius, -1);
            int borderWidth = a.getDimensionPixelSize(R.styleable.RoundedImageView_rivBorderWidth, -1);
            if (cornerRadius < 0) {
                cornerRadius = DEFAULT_RADIUS;
            }
            if (borderWidth < 0) {
                borderWidth = DEFAULT_BORDER_WIDTH;
            }
            int borderColor = a.getColor(R.styleable.RoundedImageView_rivBorderColor, DEFAULT_BORDER_COLOR);
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(cornerRadius);
            roundingParams.setBorder(borderColor, borderWidth);
            GenericDraweeHierarchy h = getHierarchy();
            if (h == null) {
                GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
                h = builder.setRoundingParams(roundingParams).build();
            } else {
                h.setRoundingParams(roundingParams);
            }
            return h;
        } catch (Exception e) {
            AppLog.e(TAG, "circle image get attr error, e:" + e.getLocalizedMessage());
        } finally {
            a.recycle();
        }
        return getHierarchy();
    }


}
