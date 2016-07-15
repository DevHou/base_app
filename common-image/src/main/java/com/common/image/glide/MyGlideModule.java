package com.common.image.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.target.ViewTarget;
import com.common.image.R;

/**
 * Created by houlijiang on 16/7/14.
 * 
 * 给glide设置setTag的id，避免外部使用时不能setTag
 */
public class MyGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        ViewTarget.setTagId(R.id.glide_image_tag_id);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
