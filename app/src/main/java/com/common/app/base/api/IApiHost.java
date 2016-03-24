package com.common.app.base.api;


import com.common.app.base.manager.DeployManager;

/**
 * Created by houlijiang on 15/12/4.
 *
 * 每个子模块需要实现的接口
 */
public interface IApiHost {

    /**
     * 根据环境返回域名
     * 
     * @param type 环境类型
     * @return 域名
     */
    String getHost(DeployManager.EnvironmentType type);
}
