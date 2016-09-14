package com.common.app.ui.test;

import android.os.Bundle;

import com.common.app.R;
import com.common.app.ui.BaseActivity;
import com.common.image.CommonImageView;
import com.common.image.ImageLoader;
import com.common.image.ImageOptions;
import com.common.utils.FileUtils;

import java.io.File;

/**
 * Created by houlijiang on 15/12/23.
 * 
 * 测试图片加载
 */
public class TestImageLoaderActivity extends BaseActivity {

    private static final String TAG = TestImageLoaderActivity.class.getSimpleName();

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_image_loader);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showBackBtn();
        setTitle("测试图片库");

        ImageOptions options =
            new ImageOptions.Builder().showEmptyImage(R.drawable.ic_list_empty).showFailImage(R.drawable.ic_list_fail)
                .showLoadingImage(R.drawable.ic_list_empty).build();

        // CommonImageView drawableCiv = (CommonImageView) findViewById(R.id.test_image_loader_iv_drawable_circle);
        // ImageLoader.displayImage(R.drawable.test_image_loader_pic, drawableCiv, options);

        CommonImageView netIv = (CommonImageView) findViewById(R.id.test_image_loader_iv_net);
        ImageOptions.ImageSize size = new ImageOptions.ImageSize(200, 200);
        options.setImageSize(size);

        options.setIsGif(true);
        String url = "http://img4.imgtn.bdimg.com/it/u=2267640641,1376734823&fm=21&gp=0.jpg";
        ImageLoader.displayImage(this, url, netIv, options);
        CommonImageView netCiv = (CommonImageView) findViewById(R.id.test_image_loader_iv_net_circle);
        ImageLoader.displayImage(this, url, netCiv, null);
        CommonImageView netRiv = (CommonImageView) findViewById(R.id.test_image_loader_iv_net_round);
        ImageLoader.displayImage(this, url, netRiv, null);

        File imageFile = new File(FileUtils.getLargeSDCardPath() + File.separator + "GSX/20151221074051.jpg");
        CommonImageView fileIv = (CommonImageView) findViewById(R.id.test_image_loader_iv_file);
        ImageLoader.displayImage(this, imageFile, fileIv, options);
    }
}
