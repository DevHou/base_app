package com.common.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by houlijiang on 16/1/20.
 * 
 * 任务分发工具类
 * 可以将runnable分发到主线程或者后台线程
 */
public class DispatchUtils {

    private final Executor mMainPoster;
    private final Executor mBackgroundPoster = Executors.newFixedThreadPool(2);

    private DispatchUtils() {
        mMainPoster = new Executor() {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void execute(Runnable runnable) {
                handler.post(runnable);
            }
        };

    }

    public static class Holder {
        public final static DispatchUtils utils = new DispatchUtils();
    }

    /**
     * 获取单例
     */
    public static DispatchUtils getInstance() {
        return Holder.utils;
    }

    /**
     * 提交到主线程执行
     */
    public void postInMain(Runnable runnable) {
        mMainPoster.execute(runnable);
    }

    /**
     * 提交到后台线程执行
     */
    public void postInBackground(Runnable runnable) {
        mBackgroundPoster.execute(runnable);
    }

}
