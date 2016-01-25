package com.houlijiang.app.base.manager;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by houlijiang on 15/11/18.
 *
 * 当前用户全局参数
 */
public class AuthManager {

    private static final String TAG = AuthManager.class.getSimpleName();

    private static class InstanceHolder {
        public final static AuthManager instance = new AuthManager();
    }

    public static AuthManager getInstance() {
        return InstanceHolder.instance;
    }

    private String authToken;
    // 登录状态改变观察者
    private final ConcurrentLinkedQueue<IAuthChangedListener> mListeners = new ConcurrentLinkedQueue<>();

    private AuthManager() {
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    /**
     * 注册环境变化回调
     */
    public boolean registerAuthChangeListener(IAuthChangedListener listener) {
        return mListeners.add(listener);
    }

    /**
     * 注销环境变化回调
     */
    public boolean unRegisterAuthChangeListener(IAuthChangedListener listener) {
        return mListeners.remove(listener);
    }

    /**
     * 退出登录时调用，清除缓存数据
     */
    public void logout() {
    }

    /**
     * 登录状态改变事件回调
     */
    public interface IAuthChangedListener {
        void onAuthChanged();
    }
}
