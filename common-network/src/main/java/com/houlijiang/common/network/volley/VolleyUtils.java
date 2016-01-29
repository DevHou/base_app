package com.houlijiang.common.network.volley;

import com.jjc.volley.AuthFailureError;
import com.jjc.volley.NetworkError;
import com.jjc.volley.NoConnectionError;
import com.jjc.volley.ParseError;
import com.jjc.volley.ServerError;
import com.jjc.volley.TimeoutError;
import com.jjc.volley.VolleyError;
import com.houlijiang.common.network.HttpResponseError;

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
        if (error instanceof TimeoutError || isNetworkProblem(error)) {
            e = new HttpResponseError(HttpResponseError.ERROR_TIMEOUT);
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
        return (error instanceof NetworkError) || (error instanceof NoConnectionError);
    }

    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError) || (error instanceof AuthFailureError);
    }

}
