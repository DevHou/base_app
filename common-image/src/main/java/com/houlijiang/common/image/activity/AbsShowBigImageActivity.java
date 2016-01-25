package com.houlijiang.common.image.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.houlijiang.common.image.ImageOptions;
import com.houlijiang.common.image.ZoomableImageView;
import com.houlijiang.common.image.utils.Utils;
import com.houlijiang.common.image.zoomableImage.zoomable.ZoomableDraweeView;

import java.io.File;

/**
 * Created by houlijiang on 15/9/7.
 * 
 * 查看大图
 */
public abstract class AbsShowBigImageActivity extends FragmentActivity {

    ZoomableDraweeView mImageView;

    /**
     * 获取layout id
     */
    abstract protected int getLayoutResource();

    /**
     * 获取zoomable mImageView ID
     */
    abstract protected int getZoomableViewId();

    /**
     * 图片加载时选项
     */
    protected ImageOptions getImageOptions() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mImageView = (ZoomableImageView) findViewById(getZoomableViewId());
    }

    protected void showImage(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        showImage(uri);
    }

    protected void showImage(File file) {
        Uri uri = Uri.fromFile(file);
        showImage(uri);
    }

    private void showImage(Uri uri) {
        ImageOptions options = getImageOptions();
        // 根据option设置显示
        if (options != null) {
            // 配置失败、加载中的显示图片
            GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
            if (options.getImageOnLoading() != null) {
                builder.setPlaceholderImage(options.getImageOnLoading(),
                    Utils.convertScaleType(options.getLoadingScaleType()));
            } else if (options.getImageResOnLoading() != 0) {
                builder.setPlaceholderImage(Utils.getDrawableFromResource(this, options.getImageResOnLoading()),
                    Utils.convertScaleType(options.getLoadingScaleType()));
            }
            if (options.getImageOnFail() != null) {
                builder.setFailureImage(options.getImageOnFail(), Utils.convertScaleType(options.getFailScaleType()));
            } else if (options.getImageResOnFail() != 0) {
                builder.setFailureImage(Utils.getDrawableFromResource(this, options.getImageResOnFail()),
                    Utils.convertScaleType(options.getFailScaleType()));
            }
            // 设置scaleType
            builder.setActualImageScaleType(Utils.convertScaleType(options.getImageScaleType()));
            // 圆角参数重新设置回去
            if (mImageView.hasHierarchy()) {
                builder.setRoundingParams(mImageView.getHierarchy().getRoundingParams());
            }
            mImageView.setHierarchy(builder.build());
        } else {
            GenericDraweeHierarchy hierarchy =
                new GenericDraweeHierarchyBuilder(getResources())
                    .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .setProgressBarImage(new ProgressBarDrawable()).build();
            mImageView.setHierarchy(hierarchy);
        }
        DraweeController ctrl = Fresco.newDraweeControllerBuilder().setUri(uri).setTapToRetryEnabled(true).build();
        mImageView.setController(ctrl);

    }
}
