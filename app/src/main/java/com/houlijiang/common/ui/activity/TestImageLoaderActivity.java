package com.houlijiang.common.ui.activity;

import android.os.Bundle;

import com.houlijiang.common.R;
import com.houlijiang.common.image.CommonImageView;
import com.houlijiang.common.image.ImageLoader;
import com.houlijiang.common.image.ImageOptions;
import com.houlijiang.common.ui.BaseActivity;
import com.houlijiang.common.utils.FileUtils;

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
            new ImageOptions.Builder().showEmptyImage(R.drawable.ic_common_list_empty)
                .showFailImage(R.drawable.ic_common_list_fail).build();

        CommonImageView netIv = (CommonImageView) findViewById(R.id.test_image_loader_iv_net);
        ImageOptions.ImageSize size = new ImageOptions.ImageSize();
        size.width = 200;
        size.height = 200;
        options.setImageSize(size);
        ImageLoader.displayImage(
            "http://g.hiphotos.baidu.com/image/pic/item/ca1349540923dd54703de864d309b3de9c82486b.jpg", netIv, options);

        File imageFile = new File(FileUtils.getLargeSDCardPath() + File.separator + "GSX/20151221074051.jpg");
        CommonImageView fileIv = (CommonImageView) findViewById(R.id.test_image_loader_iv_file);
        ImageLoader.displayImage(imageFile, fileIv, options);
    }
}
