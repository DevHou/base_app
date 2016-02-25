package com.common.app.base.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.common.app.base.utils.AppLog;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by houlijiang on 15/11/28.
 * 
 * 网络变化管理类，使用者可以主动调用，也可以通过注册回调的方式获取实时更新
 * 
 */
public class NetworkChangeManager {

    private static final String TAG = NetworkChangeManager.class.getSimpleName();

    private static ConcurrentLinkedQueue<INetChangedListener> mNetworkChangedListeners = new ConcurrentLinkedQueue<>();
    private static NetworkChangeManager instance;

    private NetworkStatus mStatus = NetworkStatus.UNKNOWN;
    private boolean mInit = false;
    private Context mContext;
    private NetworkChangeBroadcastReceiver mReceiver;

    private NetworkChangeManager() {
    }

    private static class InstanceHolder {
        public final static NetworkChangeManager instance = new NetworkChangeManager();
    }

    public static NetworkChangeManager getInstance() {
        return InstanceHolder.instance;
    }

    public void init(Context context) {
        if (mInit) {
            return;
        }
        mInit = true;
        mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.permission.ACCESS_NETWORK_STATE");
        mReceiver = new NetworkChangeBroadcastReceiver();
        context.registerReceiver(mReceiver, filter);
    }

    public void release(Context context) {
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver);
        }
    }

    /**
     * 使用者可以主动调用
     */
    public NetworkStatus getNetStatus() {
        mStatus = getNetworkStatus();
        return mStatus;
    }

    /**
     * 注册网络变化通知
     * 
     * @param listener 回调
     * @return 是否成功
     */
    public boolean registerNetChangedListener(INetChangedListener listener) {
        try {
            return mNetworkChangedListeners.add(listener);
        } catch (Exception e) {
            AppLog.e(TAG, "add listener e:" + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * 注销网络变化通知
     *
     * @param listener 回调
     * @return 是否成功
     */
    public boolean unRegisterNetChangedListener(INetChangedListener listener) {
        try {
            return mNetworkChangedListeners.remove(listener);
        } catch (Exception e) {
            AppLog.e(TAG, "remove listener e:" + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * 获取网络状态
     */
    private NetworkStatus getNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        // For WiFi
        NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        // For Cellular data
        NetworkInfo cellularDataNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiNetworkInfo.isAvailable() && wifiNetworkInfo.isConnected()) {
            return NetworkStatus.CONNECTED_WIFI;
        } else if (cellularDataNetworkInfo.isAvailable() && cellularDataNetworkInfo.isConnected()) {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            switch (tm.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return NetworkStatus.CONNECTED_GPRS;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NetworkStatus.CONNECTED_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NetworkStatus.CONNECTED_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return NetworkStatus.CONNECTED_4G;
                default:
                    return NetworkStatus.UNKNOWN;
            }
        } else {
            return NetworkStatus.DISCONNECTED;
        }
    }

    public class NetworkChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mStatus = getNetworkStatus();
            for (INetChangedListener l : mNetworkChangedListeners) {
                try {
                    l.onNetWorkChanged(mStatus);
                } catch (Exception e) {
                    AppLog.e(TAG, "notify network status changed error, e:" + e.getLocalizedMessage());
                    unRegisterNetChangedListener(l);
                }
            }
        }
    }

    /**
     * 网络状态定义
     */
    public enum NetworkStatus {
        CONNECTED_GPRS, CONNECTED_2G, CONNECTED_3G, CONNECTED_4G, CONNECTED_WIFI, DISCONNECTED, UNKNOWN
    }

    /**
     * 网络变化回调
     */
    public interface INetChangedListener {
        void onNetWorkChanged(NetworkStatus status);
    }
}
