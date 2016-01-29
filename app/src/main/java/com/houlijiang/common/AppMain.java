package com.houlijiang.common;

import android.content.Context;

import com.houlijiang.app.base.manager.DataServiceManager;
import com.houlijiang.common.listener.IApp;
import com.houlijiang.common.service.AuthDataService;

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
    }

    @Override
    public void appStop() {
        DataServiceManager.unRegisterService(AuthDataService.SERVICE_KEY);
    }
}
