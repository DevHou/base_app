package com.houlijiang.app.base.api;

/**
 * Created by houlijiang on 16/1/23.
 * 
 * 和服务器约定好的结构
 */
public class BaseApiModel {

    public long code;
    public String msg;
    public String data;
    public PageDTO pageDto;

    /**
     * 页码结构
     */
    public static class PageDTO {
        public int pageNum;// 页码 从1开始
        public int pageSize;// 每页大小
        public int curPageCount;// 当前页数据量
        public int count;// 总数
    }
}
