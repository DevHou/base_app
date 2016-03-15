package com.common.app.service;

import com.common.app.base.api.ApiResultModel;
import com.common.app.base.api.IApiCallback;
import com.common.app.base.service.BaseDataService;
import com.common.app.base.service.IDataServiceCallback;
import com.common.app.api.AuthApi;
import com.common.app.model.LoginModel;

/**
 * Created by houlijiang on 16/1/23.
 *
 * 登录等相关API
 */
public class AuthDataService extends BaseDataService {

    public static final String SERVICE_KEY = AuthDataService.class.getSimpleName();

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
}
