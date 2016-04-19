package com.common.image.fresco.photodraweeview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.common.image.CommonImageView;
import com.common.image.IOnImageClickListener;

public class FrescoPhotoView extends CommonImageView implements IAttacher {

    private Attacher mFrescoAttacher;

    public FrescoPhotoView(Context context) {
        super(context);
        init();
    }

    public FrescoPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrescoPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        if (mFrescoAttacher == null || mFrescoAttacher.getDraweeView() == null) {
            mFrescoAttacher = new Attacher(this);
        }
    }

    /**
     * 转一遍回调
     */
    @Override
    public void setImageOnClickListener(final IOnImageClickListener listener) {
        setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                listener.onImageClick(view, x, y);
            }
        });
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onImageLongClick(v);
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        canvas.concat(mFrescoAttacher.getDrawMatrix());
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mFrescoAttacher.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    public float getMinimumScale() {
        return mFrescoAttacher.getMinimumScale();
    }

    @Override
    public float getMediumScale() {
        return mFrescoAttacher.getMediumScale();
    }

    @Override
    public float getMaximumScale() {
        return mFrescoAttacher.getMaximumScale();
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        mFrescoAttacher.setMinimumScale(minimumScale);
    }

    @Override
    public void setMediumScale(float mediumScale) {
        mFrescoAttacher.setMediumScale(mediumScale);
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        mFrescoAttacher.setMaximumScale(maximumScale);
    }

    @Override
    public float getScale() {
        return mFrescoAttacher.getScale();
    }

    @Override
    public void setScale(float scale) {
        mFrescoAttacher.setScale(scale);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        mFrescoAttacher.setScale(scale, animate);
    }

    @Override
    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        mFrescoAttacher.setScale(scale, focalX, focalY, animate);
    }

    @Override
    public void setZoomTransitionDuration(long duration) {
        mFrescoAttacher.setZoomTransitionDuration(duration);
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mFrescoAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Override
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener listener) {
        mFrescoAttacher.setOnDoubleTapListener(listener);
    }

    @Override
    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        mFrescoAttacher.setOnScaleChangeListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        mFrescoAttacher.setOnLongClickListener(listener);
    }

    @Override
    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mFrescoAttacher.setOnPhotoTapListener(listener);
    }

    @Override
    public void setOnViewTapListener(OnViewTapListener listener) {
        mFrescoAttacher.setOnViewTapListener(listener);
    }

    @Override
    public OnPhotoTapListener getOnPhotoTapListener() {
        return mFrescoAttacher.getOnPhotoTapListener();
    }

    @Override
    public OnViewTapListener getOnViewTapListener() {
        return mFrescoAttacher.getOnViewTapListener();
    }

    @Override
    public void update(int imageInfoWidth, int imageInfoHeight) {
        mFrescoAttacher.update(imageInfoWidth, imageInfoHeight);
    }
}
