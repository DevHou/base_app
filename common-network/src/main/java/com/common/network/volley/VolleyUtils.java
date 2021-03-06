package com.common.network.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.common.network.HttpResponseError;

/**
 * Created by houlijiang on 14/11/19.
 *
 * volley相关
 */
public class VolleyUtils {
    /**
     * 根据不同error生成不同response error
     */
    public static HttpResponseError getResponseError(VolleyError error) {

        HttpResponseError e;
        if (error instanceof TimeoutError) {
            e = new HttpResponseError(HttpResponseError.ERROR_TIMEOUT);
        } else if (isNetworkProblem(error)) {
            e = new HttpResponseError(HttpResponseError.ERROR_CONNECT);
        } else if (error instanceof AuthFailureError) {
            e = new HttpResponseError(HttpResponseError.ERROR_AUTH);
        } else if (error instanceof ParseError) {
            e = new HttpResponseError(HttpResponseError.ERROR_PARSE);
        } else if (isServerProblem(error)) {
            e = new HttpResponseError(HttpResponseError.ERROR_SERVER_ERROR);
        } else {
            e = new HttpResponseError(HttpResponseError.ERROR_UNKNOWN);
        }
        return e;
    }

    private static boolean isNetworkProblem(Object error) {
        return (error instanceof NoConnectionError) || (error instanceof NetworkError);
    }

    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError);
    }

}
