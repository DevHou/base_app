package com.common.app.base.manager;

import android.text.TextUtils;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by houlijiang on 15/11/18.
 *
 * 当前用户全局参数
 */
public class AuthManager {

    private static final String TAG = AuthManager.class.getSimpleName();

    private static final String KEY_CURRENT_USER_AUTH = "key_current_user_auth";
    private static final String KEY_CURRENT_USER_ID = "key_current_user_id";
    private static final String KEY_CURRENT_USER_TYPE = "key_current_user_type";

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
        if (CacheManager.getInstance().contains(KEY_CURRENT_USER_AUTH)) {
            mAuthToken = CacheManager.getInstance().getString(KEY_CURRENT_USER_AUTH);
        }
        if (CacheManager.getInstance().contains(KEY_CURRENT_USER_ID)) {
            mUserId = CacheManager.getInstance().getLong(KEY_CURRENT_USER_ID);
        }
        if (CacheManager.getInstance().contains(KEY_CURRENT_USER_TYPE)) {
            mUserType = CacheManager.getInstance().getLong(KEY_CURRENT_USER_TYPE).intValue();
        }
    }

    public void setAuthToken(String authToken) {
        this.mAuthToken = authToken;
        CacheManager.getInstance().put(KEY_CURRENT_USER_AUTH, authToken);
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public void setUserId(long useId) {
        this.mUserId = useId;
        CacheManager.getInstance().put(KEY_CURRENT_USER_ID, useId);
    }

    public long getUserId() {
        return mUserId;
    }

    public int getUserType() {
        return mUserType;
    }

    public void setUserType(int userType) {
        this.mUserType = userType;
        CacheManager.getInstance().put(KEY_CURRENT_USER_TYPE, userType);
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
        mAuthToken = null;
        mUserId = 0;
        mUserType = 0;
        CacheManager.getInstance().remove(KEY_CURRENT_USER_AUTH);
        CacheManager.getInstance().remove(KEY_CURRENT_USER_ID);
        CacheManager.getInstance().remove(KEY_CURRENT_USER_TYPE);

    }

    /**
     * 登录状态改变事件回调
     */
    public interface IAuthChangedListener {
        void onAuthChanged();
    }
}
