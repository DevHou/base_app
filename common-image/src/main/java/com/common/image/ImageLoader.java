package com.common.image;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.common.image.glide.GlideImageLoader;
import com.common.image.picasso.PicassoImageLoader;

import java.io.File;

/**
 * Created by houlijiang on 14/11/21.
 * 
 * 图片加载工具类
 *
 * 切换时需要改的地方:
 * 圆角图 圆图 BigImageView的父类
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    // url后处理回调
    private static IUrlProcessor mProcessor;

    private static IImageLoader mImageLoader;
    static {
        if (BuildConfig.USE_IMAGE_LIB == BuildConstants.USE_GLIDE) {
            mImageLoader = new GlideImageLoader();
        } else {
            mImageLoader = new PicassoImageLoader();
        }
    }

    /**
     * 初始化
     *
     * @param context 上下文
     * @param cacheDir 缓存存储的目录
     */
    public static void init(Context context, File cacheDir) {
        init(context, cacheDir, false, null);
    }

    /**
     * 初始化
     *
     * @param context 上下文
     * @param cacheDir 缓存存储的目录
     */
    public static void init(Context context, File cacheDir, boolean debug) {
        init(context, cacheDir, debug, null);
    }

    /**
     * 初始化
     * 
     * @param context 上下文
     * @param cacheDir 缓存存储的目录
     */
    public static void init(Context context, File cacheDir, boolean debug, IUrlProcessor filter) {
        mImageLoader.init(context, cacheDir, debug);
        if (filter != null) {
            mProcessor = filter;
        }
    }

    /**
     * 预先缓存图片
     */
    public static void cacheImage(Context context,String url){

    }

    /**
     * 清理不需要的内存
     */
    public static void onLowMemory() {
        mImageLoader.onLowMemory();
    }

    /**
     * 获取缓存大小
     * 在后台线程
     * 
     * @return 字节数
     */
    public static long getCacheSize() {
        return mImageLoader.getCacheSize();
    }

    /**
     * 清除缓存
     * 在后台线程做
     */
    public static void clearCache() {
        mImageLoader.clearCache();
    }

    /**
     * 显示图片
     *
     * @param file 图片文件
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(Context context, File file, CommonImageView imageView, ImageOptions options) {
        displayImage(context, Uri.fromFile(file), imageView, options, null);
    }

    public static void displayImage(Activity activity, File file, CommonImageView imageView, ImageOptions options) {
        displayImage(activity, Uri.fromFile(file), imageView, options, null);
    }

    public static void displayImage(Fragment fragment, File file, CommonImageView imageView, ImageOptions options) {
        displayImage(fragment, Uri.fromFile(file), imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param file 图片文件
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(Context context, File file, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        displayImage(context, Uri.fromFile(file), imageView, options, listener);
    }

    public static void displayImage(Activity activity, File file, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        displayImage(activity, Uri.fromFile(file), imageView, options, listener);
    }

    public static void displayImage(Fragment fragment, File file, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        displayImage(fragment, Uri.fromFile(file), imageView, options, listener);
    }

    /**
     * 显示图片
     *
     * @param resId 图片drawable的id
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(Context context, int resId, CommonImageView imageView, ImageOptions options) {
        displayImage(context, resId, imageView, options, null);
    }

    public static void displayImage(Activity activity, int resId, CommonImageView imageView, ImageOptions options) {
        displayImage(activity, resId, imageView, options, null);
    }

    public static void displayImage(Fragment fragment, int resId, CommonImageView imageView, ImageOptions options) {
        displayImage(fragment, resId, imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param resId 图片drawable的id
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(Context context, int resId, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = Uri.parse("res:///" + String.valueOf(resId));
        displayImage(context, uri, imageView, options, listener);
    }

    public static void displayImage(Activity activity, int resId, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = Uri.parse("res:///" + String.valueOf(resId));
        displayImage(activity, uri, imageView, options, listener);
    }

    public static void displayImage(Fragment fragment, int resId, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = Uri.parse("res:///" + String.valueOf(resId));
        displayImage(fragment, uri, imageView, options, listener);
    }

    /**
     * 显示图片
     *
     * @param url 图片url
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     */
    public static void displayImage(Context context, String url, CommonImageView imageView, ImageOptions options) {
        displayImage(context, url, imageView, options, null);
    }

    public static void displayImage(Activity activity, String url, CommonImageView imageView, ImageOptions options) {
        displayImage(activity, url, imageView, options, null);
    }

    public static void displayImage(Fragment fragment, String url, CommonImageView imageView, ImageOptions options) {
        displayImage(fragment, url, imageView, options, null);
    }

    /**
     * 显示图片
     *
     * @param url 图片url
     * @param imageView 需要显示图片的ImageView
     * @param options 显示图片的配置项
     * @param listener 回调
     */
    public static void displayImage(Context context, String url, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = filterUri(url, imageView);
        displayImage(context, uri, imageView, options, listener);
    }

    public static void displayImage(Activity activity, String url, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = filterUri(url, imageView);
        displayImage(activity, uri, imageView, options, listener);
    }

    public static void displayImage(Fragment fragment, String url, CommonImageView imageView, ImageOptions options,
        final IImageLoadListener listener) {
        Uri uri = filterUri(url, imageView);
        displayImage(fragment, uri, imageView, options, listener);
    }

    private static Uri filterUri(String url, CommonImageView imageView) {
        Uri uri = null;
        if (!TextUtils.isEmpty(url)) {
            if (mProcessor != null) {
                url = mProcessor.filter(imageView, url);
            }
            uri = Uri.parse(url);
        }
        return uri;
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
    public static void displayImage(Context context, Uri uri, final CommonImageView imageView,
        final ImageOptions options, final IImageLoadListener listener) {
        mImageLoader.displayImage(context, uri, imageView, options, listener);
    }

    public static void displayImage(Activity activity, Uri uri, final CommonImageView imageView,
        final ImageOptions options, final IImageLoadListener listener) {
        mImageLoader.displayImage(activity, uri, imageView, options, listener);
    }

    public static void displayImage(Fragment fragment, Uri uri, final CommonImageView imageView,
        final ImageOptions options, final IImageLoadListener listener) {
        mImageLoader.displayImage(fragment, uri, imageView, options, listener);
    }

    /**
     * 对url做处理
     */
    public interface IUrlProcessor {
        String filter(CommonImageView civ, String url);
    }
}
