package com.common.app.base.api;

import android.text.TextUtils;

import com.common.app.base.manager.DeployManager;
import com.common.app.base.utils.AppLog;

/**
 * Created by houlijiang on 16/1/23.
 *
 * api层通用类，具体实现类应该继承自这个类
 */
public abstract class AbsBaseApi implements IApiHost {

    private static final String TAG = AbsBaseApi.class.getSimpleName();

    /**
     * 根据环境拼好全的url
     */
    protected String constructUrl(String api) {
        if (TextUtils.isEmpty(api)) {
            AppLog.e(TAG, "api url is null");
            return "";
        }
        if (api.startsWith("http://") || api.startsWith("https://")) {
            return api;
        }
        String host = getHost(DeployManager.getEnvironmentType());
        return host + api;
    }
}
