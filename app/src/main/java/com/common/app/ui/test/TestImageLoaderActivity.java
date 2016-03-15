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

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_image_loader);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageOptions options =
            new ImageOptions.Builder().showEmptyImage(R.drawable.ic_list_empty)
                .showFailImage(R.drawable.ic_list_fail).build();

        // CommonImageView drawableCiv = (CommonImageView) findViewById(R.id.test_image_loader_iv_drawable_circle);
        // ImageLoader.displayImage(R.drawable.test_abc, drawableCiv, options);

        CommonImageView netIv = (CommonImageView) findViewById(R.id.test_image_loader_iv_net);
        ImageOptions.ImageSize size = new ImageOptions.ImageSize(200, 200);
        options.setImageSize(size);

        String url = "http://g.hiphotos.baidu.com/image/pic/item/ca1349540923dd54703de864d309b3de9c82486b.jpg";
        ImageLoader.displayImage(url, netIv, options);
        CommonImageView netCiv = (CommonImageView) findViewById(R.id.test_image_loader_iv_net_circle);
        ImageLoader.displayImage(url, netCiv, null);
        CommonImageView netRiv = (CommonImageView) findViewById(R.id.test_image_loader_iv_net_round);
        ImageLoader.displayImage(url, netRiv, null);

        File imageFile = new File(FileUtils.getLargeSDCardPath() + File.separator + "GSX/20151221074051.jpg");
        CommonImageView fileIv = (CommonImageView) findViewById(R.id.test_image_loader_iv_file);
        ImageLoader.displayImage(imageFile, fileIv, options);
    }
}
