package com.houlijiang.common.api;

import com.houlijiang.app.base.api.AbsBaseApi;
import com.houlijiang.app.base.api.ApiUtils;
import com.houlijiang.app.base.api.IApiCallback;
import com.houlijiang.app.base.manager.DeployManager;
import com.houlijiang.common.network.HttpWorker;
import com.houlijiang.common.network.IHttpParams;

/**
 * Created by houlijiang on 16/1/23.
 * 
 * 登录相关API
 */
public class AuthApi extends AbsBaseApi {

    @Override
    public String getHost(DeployManager.EnvironmentType type) {
        switch (type) {
            case TYPE_TEST:
                return ApiConstants.AUTH_HOST_TEST;
            case TYPE_BETA:
                return ApiConstants.AUTH_HOST_BETA;
            case TYPE_ONLINE:
            default:
                return ApiConstants.AUTH_HOST_ONLINE;
        }
    }

    /**
     * 登录
     *
     * @param name 用户名
     * @param passwd 密码
     * @param listener 回调
     */
    public void login(Object origin, String name, String passwd, IApiCallback listener, Object param) {
        IHttpParams params = HttpWorker.createHttpParams();
        params.put("mobile", name);
        params.put("password", passwd);
        String url = constructUrl(ApiConstants.API_LOGIN);
        ApiUtils.doPost(origin, url, null, params, param, listener);

    }

}
