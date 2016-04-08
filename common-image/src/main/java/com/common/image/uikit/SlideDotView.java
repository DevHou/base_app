package com.common.image.uikit;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.common.image.R;

public class SlideDotView extends LinearLayout {

    private Activity ctx = null;

    private int pxDot = 0;
    private int pxDotMargin = 0;
    private int maxCount = Integer.MAX_VALUE;
    private int mTotalCount = 0;
    private int mCurrentIndex = 0;
    private int mRes_Select = R.drawable.dot_select;
    private int mRes_Unselect = R.drawable.dot_unselect;

    public SlideDotView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        if (!isInEditMode()) {
            ctx = (Activity) context;
            pxDot = ctx.getResources().getDimensionPixelSize(R.dimen.common_image_dot_size);
            pxDotMargin = ctx.getResources().getDimensionPixelSize(R.dimen.common_image_dot_margin);
        }
    }

    public void setIcon(int resSelect, int resUnSelect) {
        mRes_Select = resSelect;
        mRes_Unselect = resUnSelect;
    }

    public final void init(int i) {
        if (i < 0)
            return;

        i = Math.min(i, maxCount);

        mTotalCount = i;

        int j;
        removeAllViews();
        j = 0;

        while (j < i) {
            ImageView imageview = new ImageView(ctx);
            imageview.setImageResource(mRes_Unselect);
            imageview.setPadding(pxDotMargin, 0, pxDotMargin, 0);
            addView(imageview, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            j++;
        }

        if (i > 0) {
            ((ImageView) getChildAt(0)).setImageResource(mRes_Select);
        }
    }

    public final void setSelected(int i) {
        if (i >= getChildCount() || i < 0)
            return;

        if (maxCount != Integer.MAX_VALUE) {
            i = i % maxCount;
        }
        mCurrentIndex = i;
        int j = 0;

        while (j < getChildCount()) {
            ((ImageView) getChildAt(j)).setImageResource(mRes_Unselect);
            j++;
        }
        ((ImageView) getChildAt(i)).setImageResource(mRes_Select);
    }

    public void setMaxTotalPoint(int max) {

        if (maxCount != max) {
            maxCount = max;

            int count = Math.min(mTotalCount, max);
            this.init(count);
            this.setSelected(mCurrentIndex);
        }
    }
}
