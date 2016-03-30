package com.common.network.volley;

import com.common.network.INetCall;

import okhttp3.Call;

/**
 * Created by houlijiang on 16/3/30.
 * 
 * okHttp call
 * 
 */
public class OkHttpCall implements INetCall {

    private Call mCall;

    public OkHttpCall(Call call) {
        mCall = call;
    }

    @Override
    public void cancel() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        return mCall == null || mCall.isCanceled();
    }
}
