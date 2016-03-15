package com.common.app.base.api;

/**
 * Created by houlijiang on 15/11/28.
 *
 * API层对上层返回的数据
 */
public class ApiResultModel {

    private static final String TAG = ApiResultModel.class.getSimpleName();

    public long code;
    public String message;
    public String result;
    public PageInfo pageInfo;

    public static class PageInfo {
        public int currentPage;// 当前页码号
        public int currentPageCount;// 当前页数量
        public int pageSize;// 请求每页数量
        public int totalCount;// 数据总数
    }

    @Override
    public String toString() {
        return new StringBuffer(TAG).append(" [code:").append(code).append(", msg:").append(message).append("]")
            .toString();
    }
}
