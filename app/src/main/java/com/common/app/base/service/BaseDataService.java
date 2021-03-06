package com.common.app.base.service;

import android.text.TextUtils;

import com.common.app.base.api.ApiResultModel;
import com.common.app.base.error.ErrorConst;
import com.common.app.base.error.ErrorModel;
import com.common.app.base.model.BooleanDataModel;
import com.common.app.base.model.DataModel;
import com.common.app.base.model.ListDataModel;
import com.common.app.base.model.NullableDataModel;
import com.common.utils.AppLog;
import com.common.utils.DispatchUtils;

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

    protected static <T extends DataModel> T processApiResult(ApiResultModel result, Class<T> modelClass,
        IDataServiceCallback<T> listener, final Object param) {
        return processApiResult(result, modelClass, listener, null, param);
    }

    /**
     * 解析数据model
     * 如果需要boolean的model则一定会返回一个model对象
     *
     * @param result 网络层返回数据
     * @param modelClass 需要的model类型
     * @param listener 回调
     */
    protected static <T extends DataModel> T processApiResult(ApiResultModel result, Class<T> modelClass,
        IDataServiceCallback<T> listener, final DataPageInfo page, final Object param) {

        DataServiceResultModel serviceResult = createDefaultResultModel(result);
        T model = null;
        ErrorModel error = null;
        if (modelClass == BooleanDataModel.class) {
            model = (T) new BooleanDataModel();
            ((BooleanDataModel) model).isSuccess = false;
        } else if (NullableDataModel.class.isAssignableFrom(modelClass)) {
            try {
                model = modelClass.newInstance();
            } catch (InstantiationException e) {
                AppLog.e(TAG, "NullableDataModel newInstance e:" + e.getLocalizedMessage());
            } catch (IllegalAccessException e) {
                AppLog.e(TAG, "NullableDataModel newInstance e:" + e.getLocalizedMessage());
            }
        }
        if (result.code == ErrorConst.ERROR_CODE_SUCCESS) {
            try {
                if (!TextUtils.isEmpty(result.result) && !"\"\"".equals(result.result)) {
                    model = DataModel.doParse(result.result, modelClass);
                    if (model == null) {
                        error = ErrorModel.errorWithCode(ErrorConst.ERROR_CODE_SERVICE_PARSE_ERROR);
                    } else {
                        if (model instanceof ListDataModel) {
                            ((ListDataModel) model).pageInfo = new ListDataModel.PageInfo();
                            if (page != null) {
                                ((ListDataModel) model).pageInfo.hasMore =
                                    ((ListDataModel) model).getListCount() >= page.pageSize;
                            } else {
                                ((ListDataModel) model).pageInfo.hasMore = ((ListDataModel) model).getListCount() > 0;
                            }
                        }
                        serviceResult = DataServiceResultModel.create(ErrorConst.ERROR_CODE_SUCCESS);
                    }
                } else if ("\"\"".equals(result.result)) {
                    model = modelClass.newInstance();
                    if (ListDataModel.class.isAssignableFrom(modelClass)) {
                        ((ListDataModel) model).pageInfo = new ListDataModel.PageInfo();
                        ((ListDataModel) model).pageInfo.hasMore = false;
                    }
                    serviceResult = DataServiceResultModel.create(ErrorConst.ERROR_CODE_SUCCESS);
                } else if (modelClass == BooleanDataModel.class) {
                    // boolean类型返回值
                    ((BooleanDataModel) model).isSuccess = true;
                    serviceResult = DataServiceResultModel.create(ErrorConst.ERROR_CODE_SUCCESS, result.message);
                } else if (NullableDataModel.class.isAssignableFrom(modelClass)) {
                    // 如果是可空，则不判断data是否是空
                    serviceResult = DataServiceResultModel.create(ErrorConst.ERROR_CODE_SUCCESS, result.message);
                } else {
                    error = ErrorModel.errorWithCode(ErrorConst.ERROR_CODE_SERVICE_JSON_EMPTY);
                }
            } catch (Exception e) {
                error = ErrorModel.errorWithCode(ErrorConst.ERROR_CODE_SERVICE_PARSE_ERROR, e);
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
                if (listener != null) {
                    if (errorModel != null) {
                        try {
                            listener.onError(errorModel, param);
                        } catch (Exception e) {
                            AppLog.e(TAG, "service on data error back, e:" + e.getLocalizedMessage());
                        }
                    } else {
                        try {
                            listener.onSuccess(result, model, param);
                        } catch (Exception e) {
                            e.printStackTrace();
                            AppLog.e(TAG, "service on data success back, e:" + e.getLocalizedMessage());
                            try {
                                ErrorModel em = ErrorModel.errorWithCode(ErrorConst.ERROR_CODE_RUNTIME_CALLBACK);
                                listener.onError(em, param);
                            } catch (Exception e2) {
                                AppLog.e(TAG, "after success error, error callback e:" + e2.getLocalizedMessage());
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 直接用缓存数据model回调
     */
    protected static <T extends DataModel> void doCacheCallback(final T model, final ErrorModel errorModel,
        final IDataServiceCallback<T> listener, final Object param) {
        DataServiceResultModel m = createSuccessResultModel();
        m.isCache = true;
        if (model != null && model instanceof ListDataModel) {
            ((ListDataModel) model).pageInfo = new ListDataModel.PageInfo();
            ((ListDataModel) model).pageInfo.hasMore = false;
        }
        doCallback(m, model, errorModel, listener, param);
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

    protected class DataPageInfo {
        public int pageSize;
        public int pageNum;

        public DataPageInfo(int pageNum, int pageSize) {
            this.pageSize = pageSize;
            this.pageNum = pageNum;
        }
    }
}