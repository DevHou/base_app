package com.common.listview;

/**
 * Created by houlijiang on 15/11/23.
 *
 * 列表 cell 样式类
 */
public interface BaseListCell<T> {

    /**
     * 处理设置数据被回调
     * 
     * @param model 数据
     * @param position 数据位置
     */
    void bindData(AbsListDataAdapter.ViewHolder holder, T model, int position);

    /**
     * @return 当前 cell 的layout
     */
    int getCellViewLayout();

}
