package com.houlijiang.app.base.service;

import android.text.TextUtils;

import com.houlijiang.app.base.api.ApiResultModel;
import com.houlijiang.app.base.error.ErrorConst;
import com.houlijiang.app.base.error.ErrorModel;
import com.houlijiang.app.base.model.BooleanDataModel;
import com.houlijiang.app.base.model.DataModel;
import com.houlijiang.app.base.model.ListDataModel;
import com.houlijiang.app.base.model.NullableDataModel;
import com.houlijiang.app.base.utils.AppLog;
import com.houlijiang.common.utils.DispatchUtils;

/**
 * Created by houlijiang on 11/5/15.
 *
 * 服务器交互的服务层
 * 对服务器返回的数据进行真正的解析，并将解析后的结果返回给上层
 *
 * 子类调用对应api后将结果统一用processApiResult处理
 *
 * 回调都在主线程中
 */
public class BaseDataService {

    private static final String TAG = BaseDataService.class.getSimpleName();

    /**
     * 解析数据model
     * 如果需要boolean的model则一定会返回一个model对象
     *
     * @param result 网络层返回数据
     * @param modelClass 需要的model类型
     * @param listener 回调
     */
    protected static <T extends DataModel> T processApiResult(ApiResultModel result, Class<T> modelClass,
        IDataServiceCallback<T> listener, final Object param) {

        DataServiceResultModel serviceResult = createDefaultResultModel(result);
        T model = null;
        ErrorModel error = null;
        if (modelClass == BooleanDataModel.class) {
            model = (T) new BooleanDataModel();
            ((BooleanDataModel) model).isSuccess = false;
        } else if (modelClass == NullableDataModel.class) {
            model = (T) new NullableDataModel();
        }
        if (result.code == ErrorConst.ERROR_CODE_SUCCESS) {
            try {
                if (!TextUtils.isEmpty(result.result) && !"\"\"".equals(result.result)) {
                    model = DataModel.doParse(result.result, modelClass);
                    if (model == null) {
                        error = ErrorModel.errorWithCode(ErrorConst.ERROR_CODE_JSON_PARSE);
                    } else {
                        if (model instanceof ListDataModel) {
                            ((ListDataModel) model).pageInfo = new ListDataModel.PageInfo();
                            if (result.pageInfo != null) {
                                ((ListDataModel) model).pageInfo.currentPage = result.pageInfo.currentPage;
                                ((ListDataModel) model).pageInfo.currentPageCount = result.pageInfo.currentPageCount;
                                ((ListDataModel) model).pageInfo.pageSize = result.pageInfo.pageSize;
                                ((ListDataModel) model).pageInfo.totalCount = result.pageInfo.totalCount;
                                // 增加字段是否还有更多，判断方法是当前页返回数据数目是否等于请求的每页数据数
                                int total = result.pageInfo.currentPage * result.pageInfo.pageSize;
                                ((ListDataModel) model).pageInfo.hasMore = (total < result.pageInfo.totalCount);
                            }
                        }
                        serviceResult = DataServiceResultModel.create(ErrorConst.ERROR_CODE_SUCCESS);
                    }
                } else if (modelClass == BooleanDataModel.class) {
                    // boolean类型返回值
                    ((BooleanDataModel) model).isSuccess = true;
                    serviceResult = DataServiceResultModel.create(ErrorConst.ERROR_CODE_SUCCESS, result.message);
                } else if (modelClass == NullableDataModel.class) {
                    // 如果是可空，则不判断data是否是空
                    serviceResult = DataServiceResultModel.create(ErrorConst.ERROR_CODE_SUCCESS, result.message);
                } else {
                    error = ErrorModel.errorWithCode(ErrorConst.ERROR_SERVICE_JSON_EMPTY);
                }
            } catch (Exception e) {
                error = ErrorModel.errorWithCode(ErrorConst.ERROR_CODE_JSON_PARSE, e);
                error.print(TAG);
            }
        } else {
            error = ErrorModel.errorWithCode(result.code, result.message);
        }
        // 进行回调
        doCallback(serviceResult, model, error, listener, param);
        return model;
    }

    /**
     * 回调UI界面的回调
     * 正常从API过来的 model和error必有一个不是null
     *
     * @param result dataService的数据
     * @param model 数据结果model
     * @param errorModel 错误model
     * @param listener 回调
     * @param param 回调参数
     * @param <T> 数据类型
     */
    protected static <T extends DataModel> void doCallback(final DataServiceResultModel result, final T model,
        final ErrorModel errorModel, final IDataServiceCallback<T> listener, final Object param) {
        if (result == null || (model == null && errorModel == null)) {
            AppLog.e(TAG, "service callback model is null");
            return;
        }
        DispatchUtils.getInstance().postInMain(new Runnable() {
            @Override
            public void run() {
                try {
                    if (listener != null) {
                        if (errorModel != null) {
                            listener.onError(errorModel, param);
                        } else {
                            listener.onSuccess(result, model, param);
                        }
                    }
                } catch (Exception e) {
                    AppLog.e(TAG, "service on data back error, e:" + e.getLocalizedMessage());
                }
            }
        });
    }

    /**
     * 直接用数据model回调，给UI mock数据测试用的
     */
    protected static <T extends DataModel> void doCallback(final T model, final ErrorModel errorModel,
        final IDataServiceCallback<T> listener, final Object param) {
        doCallback(createSuccessResultModel(), model, errorModel, listener, param);
    }

    /**
     * 根据网络层返回结果构建默认服务层结果
     */
    private static DataServiceResultModel createDefaultResultModel(ApiResultModel result) {
        DataServiceResultModel serviceResult;
        if (ErrorConst.ERROR_CODE_SUCCESS == result.code) {
            serviceResult = createSuccessResultModel();
        } else {
            serviceResult = DataServiceResultModel.create(result.code, result.message);
        }
        return serviceResult;
    }

    /**
     * 创建默认成功的service层返回数据，主要是从cache取完数据后直接回调用
     */
    protected static DataServiceResultModel createSuccessResultModel() {
        return DataServiceResultModel.create(ErrorConst.ERROR_CODE_SUCCESS, "");
    }

}