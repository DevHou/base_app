package com.common.image.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.common.image.IImageLoadListener;
import com.common.image.ImageLoadError;
import com.common.image.ImageLoader;
import com.common.image.ImageOptions;
import com.common.image.photodraweeview.PhotoDraweeView;
import com.common.utils.AppLog;

import java.io.File;

/**
 * Created by houlijiang on 15/9/7.
 * 
 * 查看大图
 */
public abstract class AbsShowBigImageActivity extends AppCompatActivity {

    private static final String TAG = AbsShowBigImageActivity.class.getSimpleName();

    protected PhotoDraweeView mImageView;

    /**
     * 获取layout id
     */
    abstract protected int getLayoutResource();

    /**
     * 获取zoomable mImageView ID
     */
    abstract protected int getImageViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mImageView = (PhotoDraweeView) findViewById(getImageViewId());
    }

    protected void showImage(String url, ImageOptions options) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        showImage(uri, options);
    }

    protected void showImage(File file, ImageOptions options) {
        Uri uri = Uri.fromFile(file);
        showImage(uri, options);
    }

    private void showImage(Uri uri, ImageOptions options) {
        ImageLoader.displayImage(uri, mImageView, options, new IImageLoadListener() {
            @Override
            public void onFailed(String s, View view, ImageLoadError imageLoadError) {

            }

            @Override
            public void onSuccess(String s, View view, int width, int height) {
                if (width > 0 && height > 0) {
                    AppLog.v(TAG, "update image width:" + width + " height:" + height);
                    mImageView.update(width, height);
                }
            }
        });

    }
}
