package com.common.image.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by houlijiang on 15/9/7.
 * 
 * 图片处理用的工具类
 */
public class Utils {

    /**
     * 从资源中获取drawable
     *
     * @param id 资源ID
     * @return drawable
     */
    public static Drawable getDrawableFromResource(Context context, int id) {
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getDrawable(id);
        } else {
            drawable = context.getResources().getDrawable(id);
        }
        return drawable;
    }

}
