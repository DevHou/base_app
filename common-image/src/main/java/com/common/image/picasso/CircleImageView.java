package com.common.image.picasso;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.common.image.ImageLoader;
import com.common.image.R;
import com.common.utils.AppLog;
import com.squareup.picasso.Transformation;

/**
 * Created by houlijiang on 16/4/12.
 * 
 * 简单继承ImageView
 */
public class CircleImageView extends com.common.image.CommonImageView {

    private static final String TAG = CircleImageView.class.getSimpleName();
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private int mBorderWidth;
    private int mBorderColor;

    public CircleImageView(Context context) {
        this(context, null, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView);
        try {
            mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_civBorderWidth, DEFAULT_BORDER_WIDTH);
            mBorderColor = a.getColor(R.styleable.CircleImageView_civBorderColor, DEFAULT_BORDER_COLOR);
        } catch (Exception e) {
            AppLog.e(TAG, "circle image get attr error, e:" + e.getLocalizedMessage());
        } finally {
            a.recycle();
        }
    }

    /**
     * 加载资源图片
     *
     * @param resId 资源ID
     */
    @Override
    protected void loadImage(int resId) {
        ImageLoader.displayImage(getContext(), resId, this, null);
    }

    @Override
    public Transformation getTransform() {
        return new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                int size = Math.min(source.getWidth(), source.getHeight());

                int x = (source.getWidth() - size) / 2;
                int y = (source.getHeight() - size) / 2;

                Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
                if (squaredBitmap != source) {
                    source.recycle();
                }

                Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                BitmapShader shader =
                    new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
                paint.setShader(shader);
                paint.setAntiAlias(true);

                float r = size / 2f;
                canvas.drawCircle(r, r, r, paint);

                squaredBitmap.recycle();
                return bitmap;
            }

            @Override
            public String key() {
                return "circle";
            }
        };
    }

}
