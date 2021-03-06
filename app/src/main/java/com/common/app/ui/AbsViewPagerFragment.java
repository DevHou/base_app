package com.common.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.app.R;
import com.common.app.uikit.UnScrollViewPager;
import com.common.utils.AppLog;

/**
 * Created by houlijiang on 15/12/10.
 *
 * 带tab的viewpager
 * 
 * 有一个问题是，tabLayout高度还不能自适应，需要写死dip
 *
 * TabLayout id = common_viewpager_tl
 * ViewPager id = common_viewpager_vp
 */
public abstract class AbsViewPagerFragment extends BaseFragment {

    private static final String TAG = AbsViewPagerFragment.class.getSimpleName();

    protected UnScrollViewPager mViewPager = null;
    protected TabLayout mTabLayout = null;
    protected PagerAdapter mAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @LayoutRes
        int layoutId = getContentLayoutId() <= 0 ? R.layout.fragment_viewpager_with_title : getContentLayoutId();
        return inflater.inflate(layoutId, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTabLayout = (TabLayout) getView().findViewById(R.id.common_viewpager_tl);
        mViewPager = (UnScrollViewPager) getView().findViewById(R.id.common_viewpager_vp);
        mViewPager.setCanScroll(canScroll());
        mAdapter = new SampleFragmentPagerAdapter(getAdapterFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        updateTab();
    }

    /**
     * 获取页面资源id,返回<=0 则使用默认的
     */
    protected int getContentLayoutId() {
        return 0;
    }

    protected FragmentManager getAdapterFragmentManager() {
        return getActivity().getFragmentManager();
    }

    /**
     * 通知数据发生变化，主要用于tab数量需要从服务器获取的情况
     */
    protected void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
        updateTab();
        // mTabLayout.requestLayout();
    }

    /**
     * 更新TabLayout
     */
    private void updateTab() {
        mTabLayout.setupWithViewPager(mViewPager);
        // 使用自定义view，这个要在setupWithViewPager后，否则会被覆盖成纯文本的
        for (int i = 0; i < getCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            View view = getFragmentTabView(i, mTabLayout);
            tab.setCustomView(view);
        }
        // 设置监听，setupWithViewPager里面做了默认设置，所以要在它之后再设置一遍
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                onPageTabSelected(tab.getCustomView());
                AppLog.d(TAG, "onTabSelected:" + tab.toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                AppLog.d(TAG, "onTabUnselected:" + tab.toString());
                onPageTabUnSelected(tab.getCustomView());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                AppLog.d(TAG, "onTabReselected:" + tab.toString());
            }
        });
        // 设置默认页，因为第一页没有回调
        try {
            int curr = mViewPager.getCurrentItem();
            TabLayout.Tab tab = mTabLayout.getTabAt(curr);
            onPageTabSelected(tab.getCustomView());
        } catch (Exception e) {
            AppLog.d(TAG, "set default first tab e:" + e.getLocalizedMessage());
        }
    }

    /**
     * tab 被选中
     *
     * @param view 自定义的view
     */
    protected void onPageTabSelected(View view) {

    }

    /**
     * tab 被取消选中
     *
     * @param view 自定义view
     */
    protected void onPageTabUnSelected(View view) {

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
     * @return 当前页的标题，如果实现自定义tabview则不用重载这个方法
     */
    protected CharSequence getFragmentTitle(int position) {
        return "";
    }

    /**
     * 自定义的title view
     */
    protected View getFragmentTabView(int position, ViewGroup vg) {
        View v = LayoutInflater.from(this.getActivity()).inflate(R.layout.item_viewpager_tab, null);
        TextView tv = (TextView) v.findViewById(R.id.item_viewpager_tab_tv);
        tv.setText(getFragmentTitle(position));
        return v;
    }

    public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

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

        /**
         * 这样 notifyDataSetChanged 时会重新刷新全部页面
         * 坏处是会全部重建所有view
         */
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}
