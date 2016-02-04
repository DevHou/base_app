package com.houlijiang.app.base.manager;

import android.text.TextUtils;

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

    private long mUserId;
    private int mUserType;
    private String mAuthToken;
    // 登录状态改变观察者
    private final ConcurrentLinkedQueue<IAuthChangedListener> mListeners = new ConcurrentLinkedQueue<>();

    private AuthManager() {
    }

    public void setAuthToken(String authToken) {
        this.mAuthToken = authToken;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public void setUserId(long useId) {
        this.mUserId = useId;
    }

    public long getUserId() {
        return mUserId;
    }

    public int getmUserType() {
        return mUserType;
    }

    public void setmUserType(int mUserType) {
        this.mUserType = mUserType;
    }

    /**
     * 获取用户唯一标识
     */
    public String getUserUniqueId() {
        return mUserId + "_" + mUserType;
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(mAuthToken);
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
