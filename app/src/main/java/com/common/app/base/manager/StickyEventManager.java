package com.common.app.base.manager;

import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.text.TextUtils;

import com.common.app.base.utils.AppLog;


/**
 * Created by houlijiang on 15/12/18.
 * 
 * 事件通知，和底层EventUtils不是一个东西
 * 和ActionManager也区分开，这个主要用于一次性的事件处理，也不支持多个订阅者
 * 
 * 主要可以用来复杂情况下回传数据的，例如嵌套的fragment需要启动的activity返回数据
 */
public class StickyEventManager {

    private static final String TAG = StickyEventManager.class.getSimpleName();

    // 存储action对应的处理者
    private ConcurrentHashMap<String, IEventListener> mEventHandlers = new ConcurrentHashMap<>();
    private Handler mHandler = new Handler();

    private static class InstanceHolder {
        public final static StickyEventManager instance = new StickyEventManager();
    }

    public static StickyEventManager getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 注册事件订阅，当发生事件时直接回调
     * 
     * @param key 事件key
     * @param listener 回调处理
     */
    public void registerStickyEvent(String key, IEventListener listener) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mEventHandlers.put(key, listener);
    }

    /**
     * 通知事件发生
     * 
     * @param key 事件key
     * @param data 数据
     */
    public void sendEvent(final String key, final Object data) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        final IEventListener listener = mEventHandlers.remove(key);
        if (listener != null) {
            // 避免阻塞事件发送者
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onEventReceived(key, data);
                    } catch (Exception e) {
                        AppLog.e(TAG, "do event e:" + e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    /**
     * 事件回调接口
     */
    public interface IEventListener {
        /**
         * 事件发生
         * 
         * @param key 事件key
         * @param data 数据
         */
        void onEventReceived(String key, Object data);
    }
}
