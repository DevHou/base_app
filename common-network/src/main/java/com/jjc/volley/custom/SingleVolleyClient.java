package com.jjc.volley.custom;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.text.TextUtils;

import com.jjc.volley.Network;
import com.jjc.volley.Request;
import com.jjc.volley.RequestQueue;
import com.jjc.volley.toolbox.BasicNetwork;
import com.jjc.volley.toolbox.DiskBasedCache;
import com.jjc.volley.toolbox.HttpClientStack;
import com.jjc.volley.toolbox.HttpStack;
import com.jjc.volley.toolbox.HurlStack;

import java.io.File;
import java.security.KeyStore;

import okhttp3.OkHttpClient;

/**
 * Created by houlijiang on 14/11/18.
 * 
 * 对volley的简单封装
 */
public class SingleVolleyClient {

    private static final String TAG = SingleVolleyClient.class.getSimpleName();
    /** Default on-disk cache directory. */
    private static final String DEFAULT_CACHE_DIR = "volley-cache";

    private static SingleVolleyClient mInstance;
    private RequestQueue mRequestQueue;
    private File mCacheFile;
    private OkHttpClient mHttpClient;
    private String mUA = "volley-http";

    private SingleVolleyClient() {
    }

    public static synchronized SingleVolleyClient getInstance() {
        if (mInstance == null) {
            mInstance = new SingleVolleyClient();
        }
        return mInstance;
    }

    /**
     * 初始化
     * 
     * @param context 上下文
     * @param cacheDir 缓存目录
     * @param client httpClient
     * @param ua 请求UA
     */
    public void init(Context context, File cacheDir, OkHttpClient client, String ua) {
        if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory()) {
            cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        }
        mCacheFile = cacheDir;
        mHttpClient = client;
        if (!TextUtils.isEmpty(ua)) {
            mUA = ua;
        }
    }

    public static KeyStore getKeystore() {
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return trustStore;
    }

    /**
     * 从 Volley.newRequestQueue copy过来的，只是改了下缓存位置和userAgent名字
     */
    private RequestQueue createCustomQueue(HttpStack stack, File cache) {

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                stack = new HurlStack();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(AndroidHttpClient.newInstance(mUA));
            }
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cache), network);
        queue.start();

        return queue;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            mRequestQueue = createCustomQueue(new OkHttpStack(mHttpClient), mCacheFile);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
