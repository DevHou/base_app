package com.houlijiang.common.listview;

/**
 * Created by houlijiang on 15/3/5.
 * 
 * 右侧快速检索
 */
public interface MySectionIndexer {

    /**
     * 获取右侧索引文本
     */
    String[] getSections();

    /**
     * 根据索引文本获取第一个item的position
     */
    int getPositionForSection(int i);

    /**
     * 将position的item显示到第一的位置
     */
    void setSelection(int position);

}
