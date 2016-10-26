package com.common.app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.common.app.R;
import com.common.app.base.error.ErrorModel;
import com.common.app.base.manager.AuthManager;
import com.common.app.base.manager.DataServiceManager;
import com.common.app.base.service.DataServiceResultModel;
import com.common.app.base.service.IDataServiceCallback;
import com.common.app.model.LoginModel;
import com.common.app.service.AuthDataService;
import com.common.app.ui.BaseActivity;
import com.common.app.ui.main.bind.LoginBind;

/**
 * Created by houlijiang on 16/1/23.
 * 
 * 登录页
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private LoginBind mBinding;

    private AuthDataService mDataService = (AuthDataService) DataServiceManager.getService(AuthDataService.SERVICE_KEY);

    public static void launch(Context context) {
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
    }

    @Override
    protected boolean bindContentView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding.btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String name = mBinding.etUserName.getText().toString();
        String pwd = mBinding.etUserPwd.getText().toString();
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
