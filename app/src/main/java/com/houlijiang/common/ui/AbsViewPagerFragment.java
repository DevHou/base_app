package com.houlijiang.common.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.houlijiang.common.R;
import com.houlijiang.common.uikit.UnScrollViewPager;
import com.houlijiang.common.uikit.indicator.IPageIndicator;
import com.houlijiang.common.uikit.indicator.IIconPagerAdapter;


/**
 * Created by houlijiang on 15/12/10.
 * 
 * 带tab的viewpager
 */
public abstract class AbsViewPagerFragment extends BaseFragment implements ViewPager.OnPageChangeListener {

    private static final String TAG = AbsViewPagerFragment.class.getSimpleName();

    protected UnScrollViewPager mViewPager = null;
    protected IPageIndicator mIndicator = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layoutId = getContentLayoutId() <= 0 ? R.layout.fragment_common_viewpager_with_title : getContentLayoutId();
        View view = inflater.inflate(layoutId, container, false);

        mIndicator = (IPageIndicator) view.findViewById(R.id.common_viewpager_indicator);
        if (getIndicatorWith() > 0) {
            view.findViewById(R.id.common_viewpager_indicator).getLayoutParams().width = getIndicatorWith();
        }
        mViewPager = (UnScrollViewPager) view.findViewById(R.id.common_viewpager_vp);
        mViewPager.setCanScroll(canScroll());
        mViewPager.setAdapter(new SampleFragmentPagerAdapter(getAdapterFragmentManager()));
        mIndicator.setViewPager(mViewPager);
        mIndicator.setOnPageChangeListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 如果是indicator类型的title可以自己设定宽度
     *
     * @return 宽度，默认是0，表示match_parent
     */
    protected int getIndicatorWith() {
        return 0;
    }

    /**
     * 获取页面资源id,返回<=0 则使用默认的
     */
    protected int getContentLayoutId() {
        return 0;
    }

    protected FragmentManager getAdapterFragmentManager() {
        return getActivity().getSupportFragmentManager();
    }

    /**
     * 刷新标题
     */
    public void notifyUpdateTitle() {
        if (mIndicator != null) {
            mIndicator.notifyDataSetChanged();
        }
    }

    /**
     * 获取定制的title样式ID，默认返回0，表示除了tab没有其他元素
     */
    protected int getCustomTitleId() {
        return 0;
    }

    /**
     * 能否左右滑动切换tab，默认是可以，子类可以重写
     */
    protected boolean canScroll() {
        return true;
    }

    /**
     * @return fragment 页数
     */
    protected abstract int getCount();

    /**
     * @param position 第几页
     * @return 当前页的 fragment 实例
     */
    protected abstract Fragment getFragment(int position);

    /**
     * @return 当前页的标题
     */
    protected abstract CharSequence getFragmentTitle(int position);

    /**
     * 自定义的title view
     */
    protected View getFragmentTabView(int position) {
        return LayoutInflater.from(this.getActivity()).inflate(R.layout.item_common_viewpager_tab, null);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageSelected(int arg0) {
        Log.d(TAG, "onPageSelected i:" + arg0);
    }

    public class SampleFragmentPagerAdapter extends FragmentStatePagerAdapter implements IIconPagerAdapter {

        public SampleFragmentPagerAdapter() {
            super(getActivity().getSupportFragmentManager());
        }

        public SampleFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return AbsViewPagerFragment.this.getCount();
        }

        @Override
        public Fragment getItem(int position) {
            return AbsViewPagerFragment.this.getFragment(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return AbsViewPagerFragment.this.getFragmentTitle(position);
        }

        @Override
        public View getTabView(int index) {
            return AbsViewPagerFragment.this.getFragmentTabView(index);
        }
    }

}
