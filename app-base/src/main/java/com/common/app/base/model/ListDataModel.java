package com.common.app.base.model;

/**
 * Created by houlijiang on 11/5/15.
 * 
 * 业务层的model，所有服务器返回的model都应该继承自这个model
 * 这里的model和服务器是对应的
 *
 * 本类对应的是结果集中的 data 字段
 * 
 */
public abstract class ListDataModel extends DataModel {

    public PageInfo pageInfo;// 分页信息

    public static class PageInfo {
        public int currentPage;// 当前页码号
        public int currentPageCount;// 当前页数量
        public int pageSize;// 请求每页数量
        public int totalCount;// 数据总数
        public boolean hasMore;// 是否还有更多，不是服务器返回的
    }

}
