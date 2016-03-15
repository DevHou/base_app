package com.common.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.common.image.R;

/**
 * 圆形图片
 */
public class CircleImageView extends CommonImageView {

    private static final String TAG = CircleImageView.class.getSimpleName();
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected GenericDraweeHierarchy getInnerHierarchy(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView);
        try {
            int width = a.getDimensionPixelSize(R.styleable.CircleImageView_civBorderWidth, DEFAULT_BORDER_WIDTH);
            int color = a.getColor(R.styleable.CircleImageView_civBorderColor, DEFAULT_BORDER_COLOR);
            RoundingParams roundingParams = RoundingParams.asCircle();
            roundingParams.setBorder(color, width);
            GenericDraweeHierarchy h = getHierarchy();
            if (h == null) {
                GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
                h = builder.setRoundingParams(roundingParams).build();
            } else {
                h.setRoundingParams(roundingParams);
            }
            return h;
        } catch (Exception e) {
            Log.e(TAG, "circle image get attr error, e:" + e.getLocalizedMessage());
        } finally {
            a.recycle();
        }
        return getHierarchy();
    }

}
