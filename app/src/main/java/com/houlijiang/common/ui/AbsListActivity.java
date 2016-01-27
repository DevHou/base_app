package com.houlijiang.common.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.houlijiang.common.listview.AbsListDataAdapter;
import com.houlijiang.common.listview.AbsListView;
import com.houlijiang.common.listview.MySectionIndexer;


/**
 * Created by yanglei on 15/12/5.
 * 
 * 和Fragment一样，只是通过activity实现
 */
public abstract class AbsListActivity extends BaseActivity implements
    SwipeRefreshLayout.OnRefreshListener {

    protected View mListHeaderView;
    protected View mListEmptyView;
    protected View mListErrorView;
    protected View mListProgressView;
    protected AbsListView mRecyclerListView;
    protected AbsListDataAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();// 给子类一个机会在初始化list相关类之前初始化数据
        mAdapter = getAdapter(this);

        mRecyclerListView = (AbsListView) findViewById(getListViewId());
        mRecyclerListView.setLayoutManager(getLayoutManager());

        if (isRefreshEnabled()) {
            mRecyclerListView.setRefreshListener(this);
        }

        MySectionIndexer indexer = getIndexer();
        if (indexer != null) {
            mRecyclerListView.setIndex(indexer);
        }
        mRecyclerListView.setAdapter(mAdapter);
        loadFirstPage();

        mListHeaderView = mRecyclerListView.getHeaderView();
        mListEmptyView = mRecyclerListView.getEmptyView();
        mListErrorView = mRecyclerListView.getErrorView();
        mListProgressView = mRecyclerListView.getProgressView();
    }

    /**
     * 子类需要在初始化list相关类之前初始其他数据的可以重载这个方法
     */
    protected void initData() {
    }

    /**
     * 是否需要支持下拉刷新
     */
    protected boolean isRefreshEnabled() {
        return true;
    }

    /**
     * 下拉刷新
     * 目前什么也没做，将来可能加上一些通用逻辑，子类实现特殊逻辑
     */
    @Override
    public void onRefresh() {
        mAdapter.setIsLoading();
        onListRefresh();
    }

    /**
     * 获取ListView的adapter
     */
    protected abstract AbsListDataAdapter getAdapter(Context context);

    /**
     * 获取ListView的id
     */
    protected abstract int getListViewId();

    /**
     * 是否支持快速检索，如果返回空就是不支持
     */
    protected abstract MySectionIndexer getIndexer();

    /**
     * 获取LayoutManager
     */
    protected abstract RecyclerView.LayoutManager getLayoutManager();

    /**
     * 加载第一页
     */
    protected abstract void loadFirstPage();

    /**
     * 刷新回调
     */
    public abstract void onListRefresh();

}
