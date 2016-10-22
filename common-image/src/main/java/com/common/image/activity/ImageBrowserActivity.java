package com.common.image.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.common.image.BigImageView;
import com.common.image.IImageLoadListener;
import com.common.image.ImageLoadError;
import com.common.image.ImageLoader;
import com.common.image.ImageOptions;
import com.common.image.R;
import com.common.image.uikit.MultiTouchViewPager;
import com.common.image.uikit.SlideDotView;
import com.common.utils.AppLog;
import com.common.utils.DisplayUtils;

/**
 * Created by houlijiang on 16/1/26.
 *
 * 大图浏览页面
 */
public class ImageBrowserActivity extends FragmentActivity {

    private static final String TAG = ImageBrowserActivity.class.getSimpleName();
    protected static final String INTENT_IN_INT_DEFAULT_INDEX = "default_index";
    protected static final String INTENT_IN_STR_ARRAY_IMAGES = "images_array";
    protected static final String INTENT_IN_STR_ARRAY_SAMPLE_IMAGES = "sample_images_array";
    protected static final String INTENT_IN_SERIAL_IMAGE_OPTION = "image_option";

    private String[] mImages;
    private String[] mSampleImages;
    private ImageOptions mImageOption;
    private SlideDotView mDotView;
    private MultiTouchViewPager mViewPager;

    public static void launch(Activity context, String[] image, int initialIndex, ImageOptions options) {
        Intent intent = createIntent(context, image, initialIndex, options);
        context.startActivity(intent);
        animateInOut(context);
    }

    protected static Intent createIntent(Activity context, String[] image, int initialIndex, ImageOptions options) {
        Intent intent = new Intent(context, ImageBrowserActivity.class);
        intent.putExtra(INTENT_IN_INT_DEFAULT_INDEX, initialIndex);
        if (image != null) {
            intent.putExtra(INTENT_IN_STR_ARRAY_IMAGES, image);
        }
        if (options != null) {
            intent.putExtra(INTENT_IN_SERIAL_IMAGE_OPTION, options);
        }
        return intent;
    }

    protected static void animateInOut(Activity context) {
        context.overridePendingTransition(R.anim.common_image_brower_fade_in, R.anim.common_image_brower_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent();

        if (!getIntent().hasExtra(INTENT_IN_STR_ARRAY_IMAGES)) {
            Toast.makeText(this, getString(R.string.image_browser_image_list_empty), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        int defaultIndex = getIntent().getIntExtra(INTENT_IN_INT_DEFAULT_INDEX, 0);
        mImages = getIntent().getStringArrayExtra(INTENT_IN_STR_ARRAY_IMAGES);
        if (getIntent().hasExtra(INTENT_IN_STR_ARRAY_SAMPLE_IMAGES)) {
            mSampleImages = getIntent().getStringArrayExtra(INTENT_IN_STR_ARRAY_SAMPLE_IMAGES);
            if (mSampleImages.length != mImages.length) {
                // 数量不相等则直接置空不用
                mSampleImages = null;
                AppLog.e(TAG, "sample image url array length not equals image url array length");
            }
        }
        if (getIntent().hasExtra(INTENT_IN_SERIAL_IMAGE_OPTION)) {
            mImageOption = (ImageOptions) getIntent().getSerializableExtra(INTENT_IN_SERIAL_IMAGE_OPTION);
        }

        //
        mViewPager = getViewPager();
        mDotView = getDotView();

        PhotoImagePagerAdapter adapter = new PhotoImagePagerAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(defaultIndex);
        onImagePageSelected(defaultIndex, mImages.length);

        if (mDotView != null) {
            if (mImages.length > 1) {
                mDotView.init(mImages.length);
                mDotView.setSelected(defaultIndex);
            } else {
                mDotView.setVisibility(View.GONE);
            }
        }

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int index) {
                onImagePageSelected(index, mImages.length);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        animateInOut(this);
    }

    // ==================子类可以重载方法来定制UI=====================start
    /**
     * 设置ContentView
     */
    protected void setContent() {
        setContentView(R.layout.common_fragment_large_image);
    }

    /**
     * 获取viewPager
     */
    protected MultiTouchViewPager getViewPager() {
        return (MultiTouchViewPager) findViewById(R.id.common_fragment_large_image_view_pager);
    }

    /**
     * 获取SlideDotView
     */
    protected SlideDotView getDotView() {
        return (SlideDotView) findViewById(R.id.common_fragment_large_image_indicator);
    }

    /**
     * 页面选择
     */
    protected void onImagePageSelected(int index, int total) {
        if (mDotView != null) {
            mDotView.setSelected(index);
        }
    }

    /**
     * 生成单页view
     */
    protected View createPageView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.common_fragment_large_image_item, null);
    }

    /**
     * 获取单页中的PhotoView
     * 
     * @param view 单页View
     */
    protected BigImageView getPhotoView(View view, int position) {
        return (BigImageView) view.findViewById(R.id.common_fragment_large_image_item_photoview);
    }

    // ==================子类可以重载方法来定制UI=====================end

    private class PhotoImagePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImages.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {

            View convertView = createPageView(container.getContext());
            final BigImageView imageView = getPhotoView(convertView, position);
            // imageView.setOnPhotoTapListener(mOnPhotoTapListener);

            String url = getItem(position);

            ImageOptions options = mImageOption;
            if (options == null) {
                options = new ImageOptions();
                if (mSampleImages != null) {
                    options.setImageSample(mSampleImages[position]);
                }
            } else {
                if (mSampleImages != null) {
                    options = mImageOption.clone();
                    options.setImageSample(mSampleImages[position]);
                }
            }
            options.setImageProgress(new CustomProgressBar());

            ImageLoader.displayImage(ImageBrowserActivity.this, url, imageView, options, new IImageLoadListener() {
                @Override
                public void onFailed(String s, View view, ImageLoadError imageLoadError) {

                }

                @Override
                public void onSuccess(String s, View view, int width, int height) {
                    if (width > 0 && height > 0) {
                        AppLog.v(TAG, "update image width:" + width + " height:" + height);
                        imageView.update(width, height);
                    }
                }
            });

            // Now just add PhotoView to ViewPager and return it
            container.addView(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return convertView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public String getItem(int position) {
            return mImages[position];
        }
    }

    class CustomProgressBar extends Drawable {

        private final Paint paint;
        protected float progress;

        public CustomProgressBar() {
            this.paint = new Paint();
        }

        @Override
        public void draw(Canvas canvas) {
            final Rect bounds = getBounds();
            float density = DisplayUtils.getScreenDensity(getBaseContext());
            int size = (int) (60 * density);

            // 画圆弧背景
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((int) getResources().getDimension(R.dimen.common_image_browser_progress_width));
            paint.setColor(getResources().getColor(R.color.common_image_browser_progress_bar_bg));
            RectF oval = new RectF(); // RectF对象
            oval.left = bounds.centerX() - size / 2; // 左边
            oval.top = bounds.centerY() - size / 2; // 上边
            oval.right = bounds.centerX() + size / 2; // 右边
            oval.bottom = bounds.centerY() + size / 2;// 下边
            canvas.drawArc(oval, 0, 360, false, paint);
            // 画圆弧比例
            paint.setColor(getResources().getColor(R.color.common_image_browser_progress_bar));
            canvas.drawArc(oval, 0, 360 * progress / 100, false, paint);
            // 画中间文字
            paint.setTextSize(getResources().getDimension(R.dimen.common_image_browser_progress_text_size));
            paint.setColor(getResources().getColor(R.color.common_image_browser_progress_bar_text));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setStrokeWidth(0);
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
            float offY = fontTotalHeight / 2;
            float newY = bounds.centerY() + offY;
            canvas.drawText(progress + "%", bounds.centerX(), newY, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
            // return 1 - paint.getAlpha();
        }

        @Override
        protected boolean onLevelChange(int level) {
            progress = level / 100;
            invalidateSelf();
            return true;
        }
    }

}
