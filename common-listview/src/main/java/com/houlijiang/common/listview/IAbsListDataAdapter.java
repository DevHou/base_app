package com.houlijiang.common.listview;

/**
 * Created by houlijiang on 15/11/25.
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
}
