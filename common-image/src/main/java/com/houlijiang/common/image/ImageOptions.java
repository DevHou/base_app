package com.houlijiang.common.image;

import android.graphics.drawable.Drawable;

/**
 * Created by houlijiang on 2014/9/19.
 * 
 * image loader的配置项
 * 1、加载中，加载失败，404状态下的默认显示图片
 * 2、结果bitmap的再处理
 */
public class ImageOptions {

    private int imageResOnLoading = 0;
    private int imageResForEmptyUri = 0;
    private int imageResOnFail = 0;
    private Drawable imageOnLoading = null;
    private Drawable imageForEmptyUri = null;
    private Drawable imageOnFail = null;
    private ScaleType imageScaleType = ScaleType.CENTER_CROP;
    private ScaleType loadingScaleType = ScaleType.CENTER_CROP;
    private ScaleType emptyScaleType = ScaleType.CENTER_CROP;
    private ScaleType failScaleType = ScaleType.CENTER_CROP;
    private ImageProcessor processor = null;

    public int getImageResOnLoading() {
        return imageResOnLoading;
    }

    public void setImageResOnLoading(int imageResOnLoading) {
        this.imageResOnLoading = imageResOnLoading;
    }

    public int getImageResForEmptyUri() {
        return imageResForEmptyUri;
    }

    public void setImageResForEmptyUri(int imageResForEmptyUri) {
        this.imageResForEmptyUri = imageResForEmptyUri;
    }

    public int getImageResOnFail() {
        return imageResOnFail;
    }

    public void setImageResOnFail(int imageResOnFail) {
        this.imageResOnFail = imageResOnFail;
    }

    public Drawable getImageOnLoading() {
        return imageOnLoading;
    }

    public void setImageOnLoading(Drawable imageOnLoading) {
        this.imageOnLoading = imageOnLoading;
    }

    public Drawable getImageForEmptyUri() {
        return imageForEmptyUri;
    }

    public void setImageForEmptyUri(Drawable imageForEmptyUri) {
        this.imageForEmptyUri = imageForEmptyUri;
    }

    public Drawable getImageOnFail() {
        return imageOnFail;
    }

    public void setImageOnFail(Drawable imageOnFail) {
        this.imageOnFail = imageOnFail;
    }

    public ImageProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(ImageProcessor processor) {
        this.processor = processor;
    }

    public ScaleType getEmptyScaleType() {
        return emptyScaleType;
    }

    public void setEmptyScaleType(ScaleType emptyScaleType) {
        this.emptyScaleType = emptyScaleType;
    }

    public ScaleType getFailScaleType() {
        return failScaleType;
    }

    public void setFailScaleType(ScaleType failScaleType) {
        this.failScaleType = failScaleType;
    }

    public ScaleType getLoadingScaleType() {
        return loadingScaleType;
    }

    public void setLoadingScaleType(ScaleType loadingScaleType) {
        this.loadingScaleType = loadingScaleType;
    }

    public ScaleType getImageScaleType() {
        return imageScaleType;
    }

    public void setImageScaleType(ScaleType imageScaleType) {
        this.imageScaleType = imageScaleType;
    }

    public static class Builder {

        ImageOptions options;

        public Builder() {
            options = new ImageOptions();
        }

        public Builder showFailImage(int res) {
            options.setImageResOnFail(res);
            return this;
        }

        public Builder showLoadingImage(int res) {
            options.setImageResOnLoading(res);
            return this;
        }

        public Builder showEmptyImage(int res) {
            options.setImageResForEmptyUri(res);
            return this;
        }

        public Builder showFailImage(Drawable res) {
            options.setImageOnFail(res);
            return this;
        }

        public Builder showLoadingImage(Drawable res) {
            options.setImageOnLoading(res);
            return this;
        }

        public Builder showEmptyImage(Drawable res) {
            options.setImageForEmptyUri(res);
            return this;
        }

        public Builder imageScaleType(ScaleType t) {
            options.setImageScaleType(t);
            return this;
        }

        public Builder loadingScaleType(ScaleType t) {
            options.setLoadingScaleType(t);
            return this;
        }

        public Builder failScaleType(ScaleType t) {
            options.setFailScaleType(t);
            return this;
        }

        public Builder empptyScaleType(ScaleType t) {
            options.setEmptyScaleType(t);
            return this;
        }

        public Builder setPostProcessor(ImageProcessor p) {
            options.setProcessor(p);
            return this;
        }

        public ImageOptions build() {
            return options;
        }
    }

    public enum ScaleType {
        FIT_XY, FIT_START, FIT_CENTER, FIT_END, CENTER, CENTER_INSIDE, CENTER_CROP, FOCUS_CROP;
    }
}
