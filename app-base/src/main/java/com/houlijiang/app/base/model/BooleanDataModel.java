package com.houlijiang.app.base.model;

/**
 * Created by houlijiang on 15/12/2.
 * 
 * 空数据
 * 用于服务器只返回成功或失败的接口
 */
public class BooleanDataModel extends DataModel {
    public boolean isSuccess;// 是否成功，非服务器返回
}
