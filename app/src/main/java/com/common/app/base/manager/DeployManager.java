package com.common.app.base.manager;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.common.permission.AppPermissions;
import com.common.utils.AppLog;
import com.common.utils.AppUtils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by houlijiang on 15/11/28.
 * 
 * 设备信息以及部署环境管理
 */
public class DeployManager {

    private static final String TAG = DeployManager.class.getSimpleName();

    private static final String KEY_UUID = "key_device_uuid";

    // 环境改变观察者
    private static final ConcurrentLinkedQueue<IEnvironmentChangedListener> mListeners = new ConcurrentLinkedQueue<>();

    private static String version = "";// app 版本号
    private static String api_version = "";// api版本号
    private static String channel = "";// 当前渠道
    private static String uuid = "";// 设备唯一 id
    private static String platform = "android";
    private static String os = "android";
    private static String imei = "";// DeviceInfo.IMEI
    private static EnvironmentType environmentType = EnvironmentType.TYPE_TEST;

    public static boolean init(Context context, EnvironmentType type) {
        // 获取设备信息
        String deviceId;
        if (AppPermissions.getInstance(context).isGranted(Manifest.permission.READ_PHONE_STATE)) {
            // 有些手机提示有权限，但取时又报错没有写read_state权限声明
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                deviceId = tm.getDeviceId();
                imei = deviceId;
                AppLog.d(TAG, "imei:" + imei);
            } catch (Exception e) {
                AppLog.e(TAG, "has permission but get state e:" + e.getLocalizedMessage());
                // 设备初始化时系统生成的，每次恢复出厂设置时会变，而且手机上多个用户间也不一样
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                AppLog.d(TAG, "read from secure device id:" + deviceId);
            }
        } else {
            // 设备初始化时系统生成的，每次恢复出厂设置时会变，而且手机上多个用户间也不一样
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            AppLog.d(TAG, "read from secure device id:" + deviceId);
        }

        if (TextUtils.isEmpty(deviceId)) {
            uuid = CacheManager.getInstance().getString(KEY_UUID);
            if (TextUtils.isEmpty(uuid)) {
                uuid = UUID.randomUUID().toString();
            }
        } else {
            try {
                uuid = UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")).toString();
            } catch (UnsupportedEncodingException e) {
                AppLog.e(TAG, "create uuid error, e:" + e.getLocalizedMessage());
                uuid = UUID.randomUUID().toString();
            }
        }
        CacheManager.getInstance().put(KEY_UUID, uuid);

        version = AppUtils.getAppVersion(context);
        channel = AppUtils.getMetaData(context, "channel");
        api_version = AppUtils.getMetaData(context, "apiversion");
        platform = "android-" + Build.MANUFACTURER + ":" + Build.MODEL;
        os = "android-" + String.valueOf(Build.VERSION.SDK_INT);
        // 通知环境变化
        updateDeployType(type);
        return true;
    }

    /**
     * 更新部署环境
     * 
     * @param type 环境
     */
    public static void updateDeployType(EnvironmentType type) {
        DeployManager.environmentType = type;
        for (IEnvironmentChangedListener l : mListeners) {
            try {
                l.onEnvironmentChanged(type);
            } catch (Exception e) {
                AppLog.e(TAG, "environment changed callback error, e:" + e.getLocalizedMessage());
            }
        }
    }

    public static String getVersion() {
        return version;
    }

    public static String getApiVersion() {
        return api_version;
    }

    public static String getPlatform() {
        return platform;
    }

    public static String getChannel() {
        return channel;
    }

    public static String getUuid() {
        return uuid;
    }

    public static String getOs() {
        return os;
    }

    public static String getImei() {
        return imei;
    }

    public static EnvironmentType getEnvironmentType() {
        return environmentType;
    }

    /**
     * 注册环境变化回调
     */
    public static boolean registerEnvironmentChangeListener(IEnvironmentChangedListener listener) {
        return mListeners.add(listener);
    }

    /**
     * 注销环境变化回调
     */
    public static boolean unRegisterEnvironmentChangeListener(IEnvironmentChangedListener listener) {
        return mListeners.remove(listener);
    }

    /**
     * 连接的后端环境
     */
    public enum EnvironmentType {
        TYPE_TEST(1), TYPE_BETA(2), TYPE_ONLINE(4);

        private int value;

        public int getValue() {
            return this.value;
        }

        EnvironmentType(int value) {
            this.value = value;
        }

        public static EnvironmentType valueOf(int value) {
            switch (value) {
                case 1:
                    return TYPE_TEST;
                case 2:
                    return TYPE_BETA;
                case 4:
                    return TYPE_ONLINE;
                default:
                    return TYPE_ONLINE;
            }
        }
    }

    public interface IEnvironmentChangedListener {
        void onEnvironmentChanged(EnvironmentType type);
    }
}
