package com.common.app;

import android.content.Context;

import com.common.app.base.manager.AuthManager;
import com.common.app.base.manager.DataServiceManager;
import com.common.app.listener.IApp;
import com.common.app.service.AuthDataService;

/**
 * Created by houlijiang on 16/1/23.
 */
public class AppMain implements IApp {

    private static final String TAG = AppMain.class.getSimpleName();
    private static AppMain mInstance;

    private Context mContext;

    private AppMain(Context context) {
        mContext = context;
    }

    public static IApp getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppMain(context);
        }
        return mInstance;
    }

    @Override
    public void appStart() {
        DataServiceManager.registerService(AuthDataService.SERVICE_KEY, new AuthDataService());

        // 注册登录状态改变事件
        AuthManager.getInstance().registerAuthChangeListener(new AuthManager.IAuthChangedListener() {
            @Override
            public void onAuthChanged() {
                if (AuthManager.getInstance().isLogin()) {
                    afterLogin();
                }
            }
        });
        // 如果登录则进行登录后的初始化
        if (AuthManager.getInstance().isLogin()) {
            afterLogin();
        }
    }

    @Override
    public void appStop() {
        DataServiceManager.unRegisterService(AuthDataService.SERVICE_KEY);
    }

    /**
     * 登录后做的初始化
     */
    private void afterLogin() {
        // 刷下数据等初始化
    }
}
