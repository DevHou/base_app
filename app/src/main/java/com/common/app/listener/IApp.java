package com.common.app.listener;

/**
 * Created by houlijiang on 16/1/23.
 * 
 * application生命周期接口，每个子模块可以实现这个接口来进行初始化等
 */
public interface IApp {

    /**
     * application启动
     */
    void appStart();

    /**
     * application终止
     */
    void appStop();
}
