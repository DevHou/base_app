package com.common.app.base.manager;

import android.content.Context;
import android.text.TextUtils;

import com.common.app.base.utils.AppLog;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by houlijiang on 15/12/3.
 * 
 * 统一管理所有action分发，包括jockey过来的，schema过来的
 * 分发后支持具体实现方法进行回调，由manager再次回调回action发起方
 * 
 */
public class ActionManager {

    private static final String TAG = ActionManager.class.getSimpleName();

    // 存储action对应的处理者
    private static ConcurrentHashMap<String, IActionHandler> mActionHandlers = new ConcurrentHashMap<>();
    // 临时存储action回调，回调使用虚拟引用以免影响回调的垃圾回收
    private static ConcurrentHashMap<String, WeakReference<IActionCallback>> mActionCallbacks =
        new ConcurrentHashMap<>();

    private ActionManager() {
    }

    private static class InstanceHolder {
        public final static ActionManager instance = new ActionManager();
    }

    public static ActionManager getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 注册事件处理者
     *
     * @param action action标识string
     * @param listener 回调
     * @return 是否成功
     */
    public boolean registerActionHandler(String action, IActionHandler listener) {
        if (TextUtils.isEmpty(action)) {
            AppLog.e(TAG, "register action handler error, action is empty");
            return false;
        }
        mActionHandlers.put(action, listener);
        return true;
    }

    /**
     * 注销事件处理者
     *
     * @param action action标识string
     * @return 是否成功
     */
    public boolean unRegisterActionHandler(String action) {
        if (TextUtils.isEmpty(action)) {
            AppLog.e(TAG, "unRegister action error, action is empty");
            return false;
        }
        mActionHandlers.remove(action);
        return true;
    }

    /**
     * 发起action请求
     *
     * @param context 发起 action 跳转的界面 context
     * @param action action标识
     * @param token 唯一区分每次处理，主要是回调时发起方用来区分的
     * @param data 数据
     * @return 是否发送出去
     */
    public boolean sendToTarget(Context context, final String action, final String token, Map<String, Object> data) {
        return sendToTarget(context, action, token, data, null);
    }

    /**
     * 发起action请求
     *
     * @param context 发起 action 跳转的界面 context
     * @param action action标识
     * @param token 唯一区分每次处理，主要是回调时发起方用来区分的
     * @param data 数据
     * @param callback 回调，如果需要回调则传入，否则传空
     * @return 是否发送出去
     */
    public boolean sendToTarget(Context context, final String action, final String token, Map<String, Object> data,
        IActionCallback callback) {
        // 如果action为空则返回false
        if (TextUtils.isEmpty(action)) {
            AppLog.e(TAG, "send to target error, action is empty");
            return false;
        }
        // 如果没有注册返回false
        if (!mActionHandlers.containsKey(action)) {
            AppLog.e(TAG, "send to target error, no handler");
            return false;
        }
        // 不care token是否是空
        final String callbackKey = action + String.valueOf(token);
        // 如果传入了回调，暂时记录下来
        if (callback != null) {
            // 重复就返回false
            if (mActionCallbacks.contains(callbackKey)) {
                AppLog.e(TAG, "send to target error, contains callback key");
                return false;
            }
            mActionCallbacks.put(callbackKey, new WeakReference<>(callback));
        }
        // 调用handler并处理回调
        try {
            IActionHandler handler = mActionHandlers.get(action);
            WeakReference<Context> c = new WeakReference<>(context);
            handler.doAction(c, token, data, new IActionHandlerCallback() {
                @Override
                public void onHandlerFinish(String token, Map<String, Object> data) {
                    doActionCallback(callbackKey, action, token, data);
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, "send to target error, do action exception e:" + e.getLocalizedMessage());
            // 处理出错则使用空结果回调
            doActionCallback(callbackKey, action, token, null);
            return false;
        }
        return true;
    }

    /**
     * 回调action sender
     * 
     * @param key 暂存回调方法的key
     * @param action action标识
     * @param token 唯一表示
     * @param data 结果
     */
    private void doActionCallback(final String key, final String action, final String token,
        Map<String, Object> data) {
        try {
            WeakReference<IActionCallback> callback = mActionCallbacks.remove(key);
            if (callback != null) {
                IActionCallback c = callback.get();
                if (c != null) {
                    c.onActionDone(action, token, data);
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, "action handler callback error, e:" + e.getLocalizedMessage());
        }
    }

    /**
     * 发起action方的回调，即发起方的如果需要结果则需要传入这个实现
     */
    public interface IActionCallback {

        /**
         * action完成后的回调
         * 
         * @param token 唯一标识
         * @param data 返回的数据
         */
        void onActionDone(String action, String token, Map<String, Object> data);
    }

    /**
     * 具体处理action方需要实现的方法
     */
    public interface IActionHandler {
        /**
         * action处理者的具体实现
         *
         * @param context 调用者上下文
         * @param token 唯一标识
         * @param data 数据
         */
        void doAction(WeakReference<Context> context, String token, Map<String, Object> data,
                      IActionHandlerCallback callback);
    }

    /**
     * handler处理完成的回调，和action的回调区分开
     */
    public interface IActionHandlerCallback {

        /**
         * action处理完回调
         *
         * @param token 唯一标识
         * @param data 数据
         */
        void onHandlerFinish(String token, Map<String, Object> data);
    }
}
