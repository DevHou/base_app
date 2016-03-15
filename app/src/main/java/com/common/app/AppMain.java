package com.common.app;

import android.content.Context;

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
    }

    @Override
    public void appStop() {
        DataServiceManager.unRegisterService(AuthDataService.SERVICE_KEY);
    }
}
