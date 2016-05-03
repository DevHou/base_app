package com.common.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.common.utils.AppLog;

/**
 * 申请权限的activity，这个给AppPermission的request方法使用，外部不应该直接使用
 */
@TargetApi(Build.VERSION_CODES.M)
public class RequestPermissionActivity extends Activity {

    private static final String INTENT_IN_STR_PERMISSIONS = "permissions";

    private static final String TAG = RequestPermissionActivity.class.getSimpleName();
    private static final int CODE_REQUEST_PERMISSION = 42;

    private SharedPreferences mSharePreference;

    public static void launch(Context context, String[] permissions) {
        Intent intent = new Intent(context, RequestPermissionActivity.class);
        intent.putExtra(INTENT_IN_STR_PERMISSIONS, permissions);
        if (context instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_request_permission);
        mSharePreference = getPreferences(MODE_PRIVATE);
        if (savedInstanceState == null) {
            String[] permissions = getIntent().getStringArrayExtra(INTENT_IN_STR_PERMISSIONS);
            if (permissions == null) {
                finish();
                return;
            }
            boolean shouldShow = false;
            for (String permission : permissions) {
                // 判断是否是第一次申请权限
                if (!mSharePreference.contains(permission)) {
                    mSharePreference.edit().putBoolean(permission, true).apply();
                    shouldShow = true;
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    shouldShow = true;
                    break;
                }
            }
            if (shouldShow) {
                ActivityCompat.requestPermissions(this, permissions, CODE_REQUEST_PERMISSION);
            } else {
                int[] result = new int[permissions.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = PackageManager.PERMISSION_DENIED;
                }
                AppPermissions.getInstance(this).onRequestPermissionsResult(CODE_REQUEST_PERMISSION, permissions,
                    result);
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            AppPermissions.getInstance(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (Exception e) {
            AppLog.e(TAG, "catch exception when on permission result, ", e);
        }
        finish();
    }
}
