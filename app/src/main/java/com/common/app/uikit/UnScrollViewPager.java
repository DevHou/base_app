package com.common.app.uikit;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 禁用滑动的ViewPager，不接受滑动事件
 * 同时设置setScroll接口，可以恢复滑动事件；
 */
public class UnScrollViewPager extends ViewPager {

    private boolean unableScroll = true;

    public UnScrollViewPager(Context context) {
        super(context);
    }

    public UnScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCanScroll(boolean scroll) {
        this.unableScroll = !scroll;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        return !unableScroll && super.onTouchEvent(arg0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return !unableScroll && super.onInterceptTouchEvent(arg0);
    }
}
