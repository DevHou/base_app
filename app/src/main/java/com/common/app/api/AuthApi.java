package com.common.app.api;

import com.common.app.base.api.AbsBaseApi;
import com.common.app.base.api.ApiUtils;
import com.common.app.base.api.IApiCallback;
import com.common.app.base.manager.DeployManager;
import com.common.network.HttpWorker;
import com.common.network.IHttpParams;

/**
 * Created by houlijiang on 16/1/23.
 * 
 * 登录相关API
 */
public class AuthApi extends AbsBaseApi {

    @Override
    public String getHost(DeployManager.EnvironmentType type) {
        return ApiConstants.API_HOST;
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
