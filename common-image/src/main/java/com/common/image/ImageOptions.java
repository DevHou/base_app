package com.common.image;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by houlijiang on 2014/9/19.
 * 
 * image loader的配置项
 * 1、加载中，加载失败，404状态下的默认显示图片
 * 2、结果bitmap的再处理
 */
public class ImageOptions implements Serializable {

    private String imageSample;// 图片缩略图
    private int imageResOnLoading = 0;
    private int imageResForEmptyUri = 0;
    private int imageResOnFail = 0;
    private Drawable imageOnLoading = null;
    private Drawable imageForEmptyUri = null;
    private Drawable imageOnFail = null;
    private Drawable imageProgress = null;
    private ScaleType imageScaleType = ScaleType.FIT_CENTER;
    private ScaleType loadingScaleType = ScaleType.FIT_CENTER;
    private ScaleType emptyScaleType = ScaleType.FIT_CENTER;
    private ScaleType failScaleType = ScaleType.FIT_CENTER;
    private ImageProcessor processor = null;
    private ImageSize imageSize = null;
    private boolean isGif = false;
    private boolean isDebug = false;

    public String getImageSample() {
        return imageSample;
    }

    public void setImageSample(String imageSample) {
        this.imageSample = imageSample;
    }

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

    public ImageSize getImageSize() {
        return imageSize;
    }

    public void setImageSize(ImageSize imageSize) {
        this.imageSize = imageSize;
    }

    public Drawable getImageProgress() {
        return imageProgress;
    }

    public void setImageProgress(Drawable imageProgress) {
        this.imageProgress = imageProgress;
    }

    public boolean isGif() {
        return isGif;
    }

    public void setIsGif(boolean isGif) {
        this.isGif = isGif;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setIsDebug(boolean mIsDebug) {
        this.isDebug = mIsDebug;
    }

    @Override
    public ImageOptions clone() {
        ImageOptions options = new ImageOptions();
        options.emptyScaleType = this.emptyScaleType;
        options.failScaleType = this.failScaleType;
        options.loadingScaleType = this.loadingScaleType;
        options.imageScaleType = this.imageScaleType;
        options.imageForEmptyUri = this.imageForEmptyUri;
        options.imageResForEmptyUri = this.imageResForEmptyUri;
        options.imageOnFail = this.imageOnFail;
        options.imageResOnFail = this.imageResOnFail;
        options.imageOnLoading = this.imageOnLoading;
        options.imageResOnLoading = this.imageResOnLoading;
        options.imageProgress = this.imageProgress;
        options.imageOnFail = this.imageOnFail;
        options.imageSize = this.imageSize;
        options.processor = this.processor;
        options.isGif = this.isGif;
        options.isDebug = this.isDebug;
        return options;
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

        public Builder setIfGif(boolean isGif) {
            options.setIsGif(isGif);
            return this;
        }

        public Builder setIfDebug(boolean isDebug) {
            options.setIsDebug(isDebug);
            return this;
        }

        public ImageOptions build() {
            return options;
        }
    }

    public enum ScaleType {
        FIT_XY, FIT_START, FIT_CENTER, FIT_END, CENTER, CENTER_INSIDE, CENTER_CROP, FOCUS_CROP;
    }

    public static class ImageSize implements Serializable {
        public int width;
        public int height;

        public ImageSize() {
            this.width = 0;
            this.height = 0;
        }

        public ImageSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public ImageSize(int width, int height, int maxWidth, int maxHeight) {
            float ratio;
            if (width < maxWidth && height < maxHeight) {
                this.width = width;
                this.height = height;
            } else {
                if (maxWidth * 1.0f / width < maxHeight * 1.0f / height) {
                    ratio = maxWidth * 1.0f / width;
                } else {
                    ratio = maxHeight * 1.0f / height;
                }
                this.width = (int) (width * ratio);
                this.height = (int) (height * ratio);
            }
        }
    }
}
