package com.common.app.service;

import com.common.app.api.ApiConstants;
import com.common.app.api.AuthApi;
import com.common.app.api.TestDataModel;
import com.common.app.base.api.ApiResultModel;
import com.common.app.base.api.IApiCallback;
import com.common.app.base.manager.AuthManager;
import com.common.app.base.manager.CacheManager;
import com.common.app.base.model.ListDataModel;
import com.common.app.base.service.BaseDataService;
import com.common.app.base.service.IDataServiceCallback;
import com.common.app.model.LoginModel;
import com.common.utils.AppLog;
import com.common.utils.JsonUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by houlijiang on 16/1/23.
 *
 * 登录等相关API
 */
public class AuthDataService extends BaseDataService {

    public static final String SERVICE_KEY = AuthDataService.class.getSimpleName();

    private static final String TAG = AuthDataService.class.getSimpleName();
    private AuthApi mAuthApi;

    public AuthDataService() {
        super();
        this.mAuthApi = new AuthApi();
    }

    /**
     * 登录
     *
     * @param origin 生命周期控制对象
     * @param callback 回调
     * @param param 回调参数
     * @return 网络请求引用
     */
    public void login(Object origin, String name, String passwd, final IDataServiceCallback<LoginModel> callback,
        final Object param) {

        mAuthApi.login(origin, name, passwd, new IApiCallback() {
            @Override
            public void onRequestCompleted(ApiResultModel result, Object param) {
                processApiResult(result, LoginModel.class, callback, param);
            }
        }, param);
    }

    public void getTestList(Object origin, final boolean readCache, final int pageNum,
        final IDataServiceCallback<TestDataModel> callback, final Object param) {

        final String cacheKey = AuthManager.getInstance().getUserUniqueId() + TestDataModel.CACHE_KEY;
        if (readCache && pageNum == ApiConstants.API_LIST_FIRST_PAGE) {
            // 取缓存数据
            TestDataModel m = null;
            try {
                m = JsonUtils.parseString(CacheManager.getInstance().getString(cacheKey), TestDataModel.class);
            } catch (Exception e) {
                AppLog.e(TAG, "parse address list cache e:" + e.getLocalizedMessage());
                CacheManager.getInstance().remove(cacheKey);
            }
            if (m != null) {
                AppLog.d(TAG, "read data from cache");
                doCacheCallback(m, null, callback, param);
            }
        }

        Observable.timer(1500, TimeUnit.MILLISECONDS).observeOn(Schedulers.io()).map(new Func1<Long, TestDataModel>() {
            @Override
            public TestDataModel call(Long s) {
                final TestDataModel model = new TestDataModel();
                model.list = new TestDataModel.DataItem[2];
                for (int i = 0; i < model.list.length; i++) {
                    model.list[i] = new TestDataModel.DataItem();
                    model.list[i].name = "name" + i;
                }
                model.pageInfo = new ListDataModel.PageInfo();
                model.pageInfo.hasMore = true;
                return model;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<TestDataModel>() {
            @Override
            public void call(TestDataModel model) {
                doCallback(model, null, callback, param);
                if (pageNum == ApiConstants.API_LIST_FIRST_PAGE) {
                    try {
                        CacheManager.getInstance().put(cacheKey, JsonUtils.toString(model));
                    } catch (Exception e) {
                        AppLog.e(TAG, "cache address list e:" + e.getLocalizedMessage());
                    }
                }
            }
        });

    }
}
