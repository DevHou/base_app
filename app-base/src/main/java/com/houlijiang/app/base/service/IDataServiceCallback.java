package com.houlijiang.app.base.service;


import com.houlijiang.app.base.error.ErrorModel;
import com.houlijiang.app.base.model.DataModel;

/**
 * Created by houlijiang on 15/11/28.
 * 
 * 数据服务层回调
 */
public interface IDataServiceCallback<T extends DataModel> {

    /**
     * 回调
     * 
     * @param result service返回的数据，需要先判断result是否正确
     * @param obj 服务器数据转成的对应model
     */
    void onSuccess(DataServiceResultModel result, T obj, Object param);

    /**
     * service层返回错误
     * 
     * @param result 错误信息
     */
    void onError(ErrorModel result, Object param);
}
