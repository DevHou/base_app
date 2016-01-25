package com.houlijiang.common.uikit.indicator;

import android.view.View;

public interface IIconPagerAdapter {
    /**
     * Get icon representing the page at {@code index} in the adapter.
     */
    // int getIconResId(int index);

    View getTabView(int index);

    // From PagerAdapter
    int getCount();
}
