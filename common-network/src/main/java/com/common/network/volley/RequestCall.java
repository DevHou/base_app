package com.common.network.volley;

import com.common.network.INetCall;

/**
 * Created by houlijiang on 16/3/30.
 * 
 * volley请求的call
 */
public class RequestCall implements INetCall {

    GsonRequest mRequest;

    public RequestCall(GsonRequest req) {
        mRequest = req;
    }

    @Override
    public void cancel() {
        if (mRequest != null) {
            mRequest.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        return mRequest == null || mRequest.isCanceled();
    }
}
