/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.houlijiang.common.uikit.indicator;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.houlijiang.common.R;
import com.houlijiang.common.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class PageTabLabel extends HorizontalScrollView implements IPageIndicator {

    private int mLastSelectedItem;
    private List<TabView> mTabViewList;// hlj added
    private List<TextView> mTextViewList;// hlj added

    /**
     * Title text used when no title is provided by the adapter.
     */
    private static final CharSequence EMPTY_TITLE = "";

    /**
     * Interface for a callback when the selected tab has been reselected.
     */
    public interface OnTabReselectedListener {
        /**
         * Callback when the selected tab has been reselected.
         *
         * @param position Position of the current center item.
         */
        void onTabReselected(int position);
    }

    private Runnable mTabSelector;

    private final OnClickListener mTabClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (view instanceof TabView) {
                TabView tabView = (TabView) view;
                final int oldSelected = mViewPager.getCurrentItem();
                final int newSelected = tabView.getIndex();
                mViewPager.setCurrentItem(newSelected);
                if (oldSelected == newSelected && mTabReselectedListener != null) {
                    mTabReselectedListener.onTabReselected(newSelected);
                }
            } else {
                final int oldSelected = mViewPager.getCurrentItem();
                final int newSelected = (Integer) view.getTag();
                mViewPager.setCurrentItem(newSelected);
                if (oldSelected == newSelected && mTabReselectedListener != null) {
                    mTabReselectedListener.onTabReselected(newSelected);
                }
            }
        }
    };

    private final IcsLinearLayout mTabLayout;

    private ViewPager mViewPager;
    private OnPageChangeListener mListener;

    private int mMaxTabWidth;
    private int mSelectedTabIndex;

    private OnTabReselectedListener mTabReselectedListener;

    public PageTabLabel(Context context) {
        this(context, null);
    }

    public PageTabLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

        mLastSelectedItem = -1;
        mTabViewList = new ArrayList<>();
        mTextViewList = new ArrayList<>();
        mTabLayout = new IcsLinearLayout(context, R.attr.commonVpiTabPageIndicatorStyle);
        addView(mTabLayout, new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
    }

    public void setOnTabReselectedListener(OnTabReselectedListener listener) {
        mTabReselectedListener = listener;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
        setFillViewport(lockedExpanded);

        final int childCount = mTabLayout.getChildCount();
        if (childCount > 1 && (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST)) {
            if (childCount > 2) {
                mMaxTabWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
            } else {
                mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
            }
        } else {
            mMaxTabWidth = -1;
        }

        final int oldWidth = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int newWidth = getMeasuredWidth();

        if (lockedExpanded && oldWidth != newWidth) {
            // Recenter the tab display if we're at a new (scrollable) size.
            setCurrentItem(mSelectedTabIndex);
        }
    }

    private void animateToTab(final int position) {
        final View tabView = mTabLayout.getChildAt(position);
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
        mTabSelector = new Runnable() {
            public void run() {
                final int scrollPos = tabView.getLeft() - (getWidth() - tabView.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                mTabSelector = null;
            }
        };
        post(mTabSelector);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTabSelector != null) {
            // Re-post the selector we saved
            post(mTabSelector);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
    }

    int mDividerRes = 0;

    public void setDividerRes(int res) {
        mDividerRes = res;
    }

    int mTabCount = 0;

    public ImageView getDivider() {
        ImageView img = new ImageView(getContext());
        img.setBackgroundResource(mDividerRes);
        return img;
    }

    private void addTab(int index, CharSequence text, int iconResId) {
        final TabView tabView = new TabView(getContext());
        tabView.mIndex = index;
        tabView.setFocusable(true);
        tabView.setOnClickListener(mTabClickListener);
        tabView.setText(text);

        // if (iconResId != 0) {
        // tabView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
        // }
        tabView.setIcon(iconResId);
        mTabViewList.add(tabView);

        // mTabLayout.setDividerDrawable(mDivider);
        mTabCount++;
        mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, MATCH_PARENT, 1));
        if (mDividerRes > 0 && mTabCount != mViewPager.getAdapter().getCount()) {
            mTabLayout.addView(getDivider(), new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
        }
    }

    private void addTab(int index, View view, CharSequence text) {
        assert (view != null);

        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        if (textView == null) {
            throw new NullPointerException("Not Found TextView by R.id.tab_text.");
        }
        textView.setText(text);
        view.setOnClickListener(mTabClickListener);
        view.setTag(index);
        mTextViewList.add(textView);

        mTabCount++;
        mTabLayout.addView(view, new LinearLayout.LayoutParams(0, MATCH_PARENT, 1));
        if (mDividerRes > 0 && mTabCount != mViewPager.getAdapter().getCount()) {
            mTabLayout.addView(getDivider(), new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (mListener != null) {
            mListener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurrentItem(arg0);
        if (mListener != null) {
            mListener.onPageSelected(arg0);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        final PagerAdapter adapter = view.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        view.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mTabLayout.removeAllViews();
        mTabCount = 0;
        mTabViewList.clear();
        mTextViewList.clear();
        PagerAdapter adapter = mViewPager.getAdapter();
        IIconPagerAdapter iconAdapter = null;
        if (adapter instanceof IIconPagerAdapter) {
            iconAdapter = (IIconPagerAdapter) adapter;
        }
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            CharSequence title = adapter.getPageTitle(i);
            if (title == null) {
                title = EMPTY_TITLE;
            }
            if (iconAdapter == null) {
                addTab(i, title, 0);
            } else {
                View child = iconAdapter.getTabView(i);
                addTab(i, child, title);
            }
            // int iconResId = 0;
            // if (iconAdapter != null) {
            // iconResId = iconAdapter.getIconResId(i);
            // }
            // addTab(i, title, iconResId);
        }
        if (mSelectedTabIndex > count) {
            mSelectedTabIndex = count - 1;
        }
        setCurrentItem(mSelectedTabIndex);
        requestLayout();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mSelectedTabIndex = item;
        mViewPager.setCurrentItem(item);
        try {
            TabView tabView = mTabViewList.get(item);
            if (tabView != null) {
                tabView.setText(mViewPager.getAdapter().getPageTitle(item));
            }
            if (mLastSelectedItem >= 0) {
                tabView = mTabViewList.get(mLastSelectedItem);
                if (tabView != null) {
                    tabView.setText(mViewPager.getAdapter().getPageTitle(mLastSelectedItem));
                }
            }
        } catch (Exception e) {
        }
        try {
            TextView textView = mTextViewList.get(item);
            if (textView != null) {
                textView.setText(mViewPager.getAdapter().getPageTitle(item));
                textView.setSelected(true);
            }
            if (mLastSelectedItem >= 0 && mLastSelectedItem != mSelectedTabIndex) {
                textView = mTextViewList.get(mLastSelectedItem);
                if (textView != null) {
                    textView.setText(mViewPager.getAdapter().getPageTitle(mLastSelectedItem));
                    textView.setSelected(false);
                }
            }
        } catch (Exception e) {
        }
        mLastSelectedItem = item;

        int divider = 1;
        final int tabCount = mTabLayout.getChildCount();
        for (int i = 0; i < tabCount; i++) {
            if (mDividerRes > 0 && i % 2 == 1) {
                divider = 2;
                continue;
            }
            final View child = mTabLayout.getChildAt(i);
            final boolean isSelected = (i == item * divider);
            child.setSelected(isSelected);
            // DLog.d("@@@child " + child.findViewById(R.id.tab_text) + "  " + isSelected);
            // child.findViewById(R.id.tab_text).setSelected(isSelected);

            if (isSelected) {
                animateToTab(item * divider);
            }
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    // private class TabView extends TextView {
    // private int mIndex;
    //
    // public TabView(Context context) {
    // super(context, null, R.attr.vpiTabPageIndicatorStyle);
    // }
    //
    // @Override
    // public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //
    // // Re-measure if we went beyond our maximum size.
    // if (mMaxTabWidth > 0 && getMeasuredWidth() > mMaxTabWidth) {
    // super.onMeasure(MeasureSpec.makeMeasureSpec(mMaxTabWidth,
    // MeasureSpec.EXACTLY),
    // heightMeasureSpec);
    // }
    // }
    //
    // public int getIndex() {
    // return mIndex;
    // }
    // }

    private class TabView extends FrameLayout {
        private int mIndex;

        private TextView mTextView;
        private ImageView mImageView;

        public TabView(Context context) {
            super(context);

            mTextView = new TextView(context, null, R.attr.commonVpiTabPageIndicatorStyle);
            mImageView = new ImageView(context);
            mTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
            addView(mTextView);
            LayoutParams params = new LayoutParams(-2, -2, Gravity.RIGHT | Gravity.TOP);
            params.topMargin = (int) (8 * DisplayUtils.getScreenDensity(getContext()));
            params.rightMargin = (int) (7 * DisplayUtils.getScreenDensity(getContext()));
            mImageView.setLayoutParams(params);
            addView(mImageView);

        }

        public void setText(CharSequence text) {
            mTextView.setText(text);
        }

        public void setIcon(int resId) {
            if (resId <= 0) {
                mImageView.setVisibility(View.INVISIBLE);
            } else {
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(resId);
            }
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // Re-measure if we went beyond our maximum size.
            if (mMaxTabWidth > 0 && getMeasuredWidth() > mMaxTabWidth) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(mMaxTabWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        }

        public int getIndex() {
            return mIndex;
        }

    }
}
