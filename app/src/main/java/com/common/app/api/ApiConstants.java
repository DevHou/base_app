package com.common.app.api;

import com.common.app.base.manager.DeployManager;

/**
 * Created by houlijiang on 16/1/23.
 */
public class ApiConstants {

    public static final int API_LIST_FIRST_PAGE = 1;

    public static final String API_HOST;
    static {
        switch (DeployManager.getEnvironmentType()) {
            case TYPE_TEST: {
                API_HOST = "http://test-i.houlijiang.com/api/";
                break;
            }
            case TYPE_BETA: {
                API_HOST = "http://beta-i.houlijiang.com/api/";
                break;
            }
            case TYPE_ONLINE:
            default: {
                API_HOST = "http://i.houlijiang.com/api/";
                break;
            }
        }
    }

    public static final String API_LOGIN = "app/auth/doLogin.do";
}
