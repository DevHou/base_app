package com.houlijiang.common.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.houlijiang.app.base.error.ErrorModel;
import com.houlijiang.app.base.manager.AuthManager;
import com.houlijiang.app.base.manager.DataServiceManager;
import com.houlijiang.app.base.service.DataServiceResultModel;
import com.houlijiang.app.base.service.IDataServiceCallback;
import com.houlijiang.common.R;
import com.houlijiang.common.model.LoginModel;
import com.houlijiang.common.service.AuthDataService;
import com.houlijiang.common.ui.BaseActivity;

/**
 * Created by houlijiang on 16/1/23.
 * 
 * 登录页
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEtName;
    private EditText mEtPwd;

    private AuthDataService mDataService = (AuthDataService) DataServiceManager.getService(AuthDataService.SERVICE_KEY);

    public static void launch(Context context) {
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
    }

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_common_login);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEtName = (EditText) findViewById(R.id.login_et_user_name);
        mEtPwd = (EditText) findViewById(R.id.login_et_user_pwd);
        findViewById(R.id.login_btn_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String name = mEtName.getText().toString();
        String pwd = mEtPwd.getText().toString();
        mDataService.login(this, name, pwd, new IDataServiceCallback<LoginModel>() {
            @Override
            public void onSuccess(DataServiceResultModel result, LoginModel obj, Object param) {
                AuthManager.getInstance().setAuthToken(obj.auth_token);
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(ErrorModel result, Object param) {
                Toast.makeText(LoginActivity.this, "登录失败" + result.message, Toast.LENGTH_SHORT).show();
            }
        }, null);
    }
}
