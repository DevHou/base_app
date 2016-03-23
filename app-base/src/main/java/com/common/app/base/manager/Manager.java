package com.common.app.base.manager;

import android.content.Context;

import com.common.app.base.error.ErrorConst;
import com.common.image.ImageLoader;
import com.common.network.HttpWorker;
import com.common.utils.FileUtils;
import com.common.utils.ResourceManager;

import java.io.File;

/**
 * Created by houlijiang on 15/12/2.
 * 
 * 所有manager的统一管理，主要是进行初始化操作
 */
public class Manager {

    private static final String TAG = Manager.class.getSimpleName();

    /**
     * 只在主进程中初始化
     *
     * 主要初始化主页面相关
     */
    public static boolean initForMain(Context context) {
        return true;
    }

    /**
     * 释放资源
     */
    public static boolean release(Context context) {
        NetworkChangeManager.getInstance().release(context);
        DownloadManager.getInstance().release();
        UploadManager.getInstance().release();
        ResourceManager.getInstance().release();
        return true;
    }

    /**
     * 每个进程中都要初始化的
     * 
     * 初始化基础模块，需要在多个进程中都初始化
     * 注意 这里初始化必须使用内部存储，因为6.0可能没有外部存储权限，如果实时获取权限，代码逻辑不好处理
     */
    public static boolean initForProcess(Context context, DeployManager.EnvironmentType type) {
        // 配置环境
        DeployManager.init(context, type);
        // 初始化错误模块
        ErrorConst.init(context);

        String goodCacheDir = FileUtils.tryGetGoodDiskCacheDir(context);
        // 缓存初始化，需要比较稳定的存储位置
        String netCacheDir = goodCacheDir + File.separator + "netCache";
        FileUtils.createDirIfNotExists(netCacheDir);
        File netCache = new File(netCacheDir);
        HttpWorker.init(context, netCache, 20000);
        // 缓存初始化，需要比较稳定的存储位置
        String dataCacheDir = goodCacheDir + File.separator + "dataCache";
        FileUtils.createDirIfNotExists(dataCacheDir);
        File dataCache = new File(dataCacheDir);
        CacheManager.getInstance().init(context, dataCache);
        // 图片库初始化
        String imageCacheDir = goodCacheDir + File.separator + "imageCache";
        FileUtils.createDirIfNotExists(imageCacheDir);
        File imageCache = new File(imageCacheDir);
        ImageLoader.init(context, imageCache);
        // 网络变化监听初始化
        NetworkChangeManager.getInstance().init(context);
        // 资源管理器初始化
        ResourceManager.getInstance().init();
        // 下载管理器
        DownloadManager.getInstance().init();
        // 上传管理器
        UploadManager.getInstance().init();
        return true;
    }
}
