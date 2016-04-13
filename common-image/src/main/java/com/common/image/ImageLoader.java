package com.common.image;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.common.image.glide.GlideImageLoader;

import java.io.File;

/**
 * Created by houlijiang on 14/11/21.
 * 
 * 图片加载工具类
 *
 * 切换时需要改的地方
 * 此类的mImageLoader实例
 * 圆角图 圆图 BigImageView的父类
 * 具体实现里的圆角图 原图里的重载方法
 * 具体ImageLoader实现的displayImage的imageView变量
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    // url后处理回调
    private static IUrlProcessor mProcessor;

    // private static FrescoImageLoader mImageLoader = new FrescoImageLoader();

    private static GlideImageLoader mImageLoader = new GlideImageLoader();

    /**
     * 初始化
     *
     * @param context 上下文
     * @param cacheDir 缓存存储的目录
     */
    public static void init(Context context, File cacheDir) {
        init(context, cacheDir, null);
    }

    /**
     * 初始化
     * 
     * @param context 上下文
     * @param cacheDir 缓存存储的目录
     */
    public static void init(Context context, File cacheDir, IUrlProcessor filter) {
        mImageLoader.init(context, cacheDir);
        if (filter != null) {
            mProcessor = filter;
        }
    }

    /**
     * 显示图片
     *
     * @param file 图片文件
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(File file, CommonImageView imageView, ImageOptions options) {
        displayImage(Uri.fromFile(file), imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param file 图片文件
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(File file, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        displayImage(Uri.fromFile(file), imageView, options, listener);
    }

    /**
     * 显示图片
     *
     * @param resId 图片drawable的id
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(int resId, CommonImageView imageView, ImageOptions options) {
        Uri uri = Uri.parse("res:///" + String.valueOf(resId));
        displayImage(uri, imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param resId 图片drawable的id
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(int resId, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = Uri.parse("res:///" + String.valueOf(resId));
        displayImage(uri, imageView, options, listener);
    }

    /**
     * 显示图片
     *
     * @param url 图片url
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(String url, CommonImageView imageView, ImageOptions options) {
        displayImage(url, imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param url 图片url
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     * @param listener 回调
     */
    public static void displayImage(String url, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = null;
        if (!TextUtils.isEmpty(url)) {
            if (mProcessor != null) {
                url = mProcessor.filter(imageView, url);
            }
            uri = Uri.parse(url);
        }
        displayImage(uri, imageView, options, listener);
    }

    /**
     * 显示图片
     * 
     * Scheme Fetch method used
     * File on network http://, https:// HttpURLConnection or network layer
     * File on device file:// FileInputStream
     * Content provider content:// ContentResolver
     * Asset in app asset:// AssetManager
     * Resource in app res:// as in res://12345 Resources.openRawResource
     * Data in URI data:mime/type;base64, Following data URI spec (UTF-8 only)
     *
     * @param uri 图片uri，可以是file drawable assets url
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     * @param listener 回调
     */
    public static void displayImage(Uri uri, final CommonImageView imageView, final ImageOptions options,
        final IImageLoadListener listener) {
        mImageLoader.displayImage(uri, imageView, options, listener);
    }

    /**
     * 对url做处理
     */
    public interface IUrlProcessor {
        String filter(CommonImageView civ, String url);
    }
}
