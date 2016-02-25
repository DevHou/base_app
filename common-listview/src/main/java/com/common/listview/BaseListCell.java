package com.common.listview;

import android.view.View;

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
    void setData(T model, int position);

    /**
     * @return 当前 cell 的布局文件 id
     */
    int getCellResource();

    /**
     * 初始化 cell 的各个子 view。
     * 
     * @param view 根view
     */
    void initialChildViews(View view);
}
