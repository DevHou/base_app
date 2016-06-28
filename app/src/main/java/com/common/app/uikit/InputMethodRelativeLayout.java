package com.common.app.uikit;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.widget.RelativeLayout;

/**
 * Created by houlijiang on 15/3/24.
 * 
 * 自定义布局解决键盘弹出挡住输入框的问题
 */
public class InputMethodRelativeLayout extends RelativeLayout {
    private static final String TAG = InputMethodRelativeLayout.class.getSimpleName();

    private int width;
    protected OnSizeChangedListener onSizeChangedListenner;
    private boolean flag = false; // 变化的标志
    private int height;

    private int screenHeight; // 屏幕高度

    public InputMethodRelativeLayout(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        Display localDisplay = ((Activity) paramContext).getWindowManager().getDefaultDisplay();
        this.screenHeight = localDisplay.getHeight();
    }

    public InputMethodRelativeLayout(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.width = widthMeasureSpec;
        this.height = heightMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        // 监听不为空、宽度不变、当前高度与历史高度不为0
        if ((this.onSizeChangedListenner != null) && (w == oldw) && (oldw != 0) && (oldh != 0)) {
            if ((Math.abs(h - oldh) <= this.screenHeight / 4)) {
                return;
            }
            this.flag = h < oldh;
            Log.d(TAG, "size change set flag:" + flag);
            this.onSizeChangedListenner.onSizeChange(this.flag, oldh, h);
            measure(this.width, this.height);
        }
    }

    /**
     * 设置视图偏移的监听事件
     *
     * @param listener 监听
     */
    public void setOnSizeChangedListenner(OnSizeChangedListener listener) {
        this.onSizeChangedListenner = listener;
    }

    /**
     * 视图偏移的内部接口
     */
    public interface OnSizeChangedListener {
        void onSizeChange(boolean paramBoolean, int oldh, int h);
    }
}
