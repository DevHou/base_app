package com.common.listview;

/**
 * Created by houlijiang on 15/11/25.
 * 
 * AbsListView 数据adapter需要实现的接口，用于AbsListView里判断显示view用
 */
public interface IAbsListDataAdapter {

    /**
     * 是否在加载数据中
     */
    boolean isReloading();

    /**
     * 是否是空
     */
    boolean isEmpty();

    /**
     * 设置加载更多view
     */
    void setLoadMoreView(int resourceId);
}
