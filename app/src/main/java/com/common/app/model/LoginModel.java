package com.common.app.model;

import android.support.annotation.Keep;

import com.common.app.base.model.DataModel;

/**
 * Created by houlijiang on 16/1/23.
 */
@Keep
public class LoginModel extends DataModel {

    public long user_id;
    public long user_number;
    public String auth_token;

}
