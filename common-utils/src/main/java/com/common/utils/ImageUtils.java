package com.common.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by houlijiang on 2014/9/19.
 * 
 * 图片处理相关操作
 */
public class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();

    /**
     * 从资源中获取bitmap
     * 
     * @param context 上下文
     * @param id 资源id
     * @return bitmap
     */
    public static Bitmap getBitmapFromDrawable(Context context, int id) {
        return BitmapFactory.decodeResource(context.getResources(), id);
    }

    /**
     * 从URI中获取bitmap
     * 
     * @param context 上下文
     * @param contentUri 资源位置
     * @return bitmap
     */
    public static Bitmap getBitmapFromContent(Context context, Uri contentUri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(contentUri);
            Bitmap bt = null;
            if (is != null) {
                bt = BitmapFactory.decodeStream(is);
                is.close();
            }
            return bt;
        } catch (FileNotFoundException e) {
            AppLog.e(TAG, "file not found exception when open content");
        } catch (IOException e) {
            AppLog.e(TAG, "io exception when open content");
        } catch (Exception e) {
            AppLog.e(TAG, "exception when open content");
        }
        return null;
    }

    /**
     * 获取图片uri真实路径
     * 
     * @param context 上下文
     * @param contentUri 资源uri
     * @return 路径
     */
    public static String getRealPathFromURI(Activity context, Uri contentUri) {
        String imagePath = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Cursor cursor = context.managedQuery(contentUri, proj, null, null, null);
                if (cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    imagePath = cursor.getString(column_index);
                }
                cursor.close();
            } else {
                Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                if (cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    imagePath = cursor.getString(column_index);
                }
                cursor.close();
            }
        } catch (Exception e) {
            AppLog.e(TAG, "catch exception when query uri, e:" + e.getLocalizedMessage());
        }
        return imagePath;

    }

    /**
     * 压缩图片
     * 
     * @param path 原始图片路径
     * @param width 需要的宽度 0表示随着height等比
     * @param height 需要的高度 0表示随着width等比
     * @return bitmap
     */
    public static Bitmap getThumbnail(String path, int width, int height) {
        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        int scale = getPicScale(path, width, height);
        return getThumbnail(path, scale);
    }

    /**
     * 压缩图片
     * 
     * @param path 图片文件路径
     * @param scale 压缩比例
     * @return bitmap
     */
    public static Bitmap getThumbnail(String path, int scale) {
        Bitmap b;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        b = BitmapFactory.decodeFile(path, options);
        if (b == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        b.recycle();
        b = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
        return b;
    }

    public static Bitmap getThumbnail(Context context, Uri uri, int width, int height) {
        try {
            int scale = getPicScale(context, uri, width, height);
            return getThumbnail(context, uri, scale);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getThumbnail(Context context, Uri uri, int scale) throws FileNotFoundException {

        Bitmap b = null;
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            b = BitmapFactory.decodeStream(is, null, options);
            if (b == null) {
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            b.recycle();
            b = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
        } finally {
            IOUtils.closeSilently(is);
        }
        return b;
    }

    /**
     * 压缩图片
     * 
     * @param bitmap 原始图片
     * @param width 需要的宽度 0表示随着height等比
     * @param height 需要的高度 0表示随着width等比
     * @return bitmap
     */
    public static Bitmap getThumbnail(Bitmap bitmap, int width, int height) {
        Bitmap b;
        if (bitmap.getHeight() != height || bitmap.getWidth() != width) {
            b = Bitmap.createScaledBitmap(bitmap, width, height, true);
            bitmap.recycle();
        } else {
            b = bitmap;
        }
        return b;
    }

    /**
     * 将图片存入文件
     * 
     * @param bitmap 图片
     * @param outPath 存储路径
     * @return 是否成功
     */
    public static boolean BitmapToFile(Bitmap bitmap, String outPath) {
        if (bitmap == null) {
            return false;
        }
        File newFile = new File(outPath);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            AppLog.e(TAG, "e:" + e.getLocalizedMessage());
        } catch (IOException e) {
            AppLog.e(TAG, "e:" + e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * 直接将某个图片压缩后存到新的路径
     * 
     * @param inPath 输入图片路径
     * @param outPath 输出图片路径
     * @param width 需要的宽度 0表示随着height等比
     * @param height 需要的高度 0表示随着width等比
     * @return 是否成功
     */
    public static boolean thumbnailToFile(String inPath, String outPath, int width, int height) {
        int scale = getPicScale(inPath, width, height);
        return thumbnailToFile(inPath, outPath, scale);
    }

    /**
     * 直接将某个图片压缩后存到新的路径
     * 
     * @param inPath 输入图片路径
     * @param outPath 输出图片路径
     * @param scale 压缩比例
     * @return 是否成功
     */
    public static boolean thumbnailToFile(String inPath, String outPath, int scale) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(inPath, options);
        return BitmapToFile(bitmap, outPath);
    }

    /**
     * 根据需要高宽计算压缩比例
     * 
     * @param path 输入图片路径
     * @param width 需要的宽度 0表示随着height等比
     * @param height 需要的高度 0表示随着width等比
     * @return 压缩比例 >=1 的值
     */
    public static int getPicScale(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return getScale(options.outWidth, options.outHeight, width, height);
    }

    /**
     * 根据需要高宽计算压缩比例
     *
     * @param bitmap 输入图片
     * @param width 需要的宽度 0表示随着height等比
     * @param height 需要的高度 0表示随着width等比
     * @return 压缩比例 >=1 的值
     */
    public static int getPicScale(Bitmap bitmap, int width, int height) {
        if (bitmap == null) {
            return 1;
        }
        return getScale(bitmap.getWidth(), bitmap.getHeight(), width, height);
    }

    /**
     * 计算压缩比例
     * 
     * @param oriW 原始宽
     * @param oriH 原始高
     * @param w 需要的宽
     * @param h 需要的高
     * @return 压缩比例 >=1 的值
     */
    private static int getScale(int oriW, int oriH, int w, int h) {
        int scale = 1;
        if (w > 0 && oriW > w) {
            scale = Math.round((float) oriW / (float) w);
        }
        if (h > 0 && oriH > h) {
            int scale2 = Math.round((float) oriH / (float) h);
            if (scale2 > scale) {
                scale = scale2;
            }
        }
        return scale;
    }

    public static int getPicScale(Context context, Uri uri, int width, int height) throws FileNotFoundException {
        int scale = 1;

        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            if (width > 0 && options.outWidth > width) {
                scale = Math.round((float) options.outWidth / (float) width);
            }
            if (height > 0 && options.outHeight > height) {
                int scale2 = Math.round((float) options.outHeight / (float) height);
                if (scale2 > scale) {
                    scale = scale2;
                }
            }
        } finally {
            IOUtils.closeSilently(is);
        }
        return scale;
    }

    /**
     * 获取图片大小
     * 
     * @param path 输入图片路径
     * @return 大小
     */
    public static PicSize getPicSize(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        boolean ifRotate = false;
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                case ExifInterface.ORIENTATION_ROTATE_270:
                    ifRotate = true;
                    break;
            }
        } catch (IOException e) {
            AppLog.e(TAG, "catch exception when get exif for path:" + path);
        }
        if (ifRotate) {
            return new PicSize(options.outHeight, options.outWidth);
        } else {
            return new PicSize(options.outWidth, options.outHeight);
        }
    }

    /**
     * 图片去色,返回灰度图片
     *
     * @param bmpOriginal 传入的图片
     * @return 去色后的图片
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * 保存图片到系统相册
     * 
     * @param imagePath 图片路径
     * @param title 图片名字
     * @param description 图片描述
     * @return 图片存储uri
     */
    public static Uri saveImageToGallery(ContentResolver cr, String imagePath, String title, String description) {
        ContentValues v = new ContentValues();
        v.put(MediaStore.Images.Media.TITLE, title);
        v.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        v.put(MediaStore.Images.Media.DESCRIPTION, description);
        long dateTaken = System.currentTimeMillis();
        v.put(MediaStore.Images.Media.DATE_ADDED, dateTaken);
        v.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
        v.put(MediaStore.Images.Media.DATE_MODIFIED, dateTaken);
        v.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        File f = new File(imagePath);
        File parent = f.getParentFile();
        String path = parent.toString().toLowerCase();
        String name = parent.getName().toLowerCase();
        v.put(MediaStore.Images.ImageColumns.BUCKET_ID, path.hashCode());
        v.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
        v.put(MediaStore.Images.Media.SIZE, f.length());
        v.put(MediaStore.MediaColumns.DATA, imagePath);
        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
    }

    /**
     * A copy of the Android internals insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     * 
     * @see MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String)
     */
    public static String insertImage(ContentResolver cr, Bitmap source, String title, String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null; /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 90, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb =
                    MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     *
     * @see MediaStore.Images.Media (StoreThumbnail private method)
     */
    private static Bitmap storeThumbnail(ContentResolver cr, Bitmap source, long id, float width, float height, int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND, kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, (int) id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * 合并两个bitmap
     * 
     * @param bmp1 底部的
     * @param bmp2 顶部的
     * @return 新的
     */
    public static Bitmap mergeBitmp(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        int left = (bmp1.getWidth() - bmp2.getWidth()) / 2;
        int top = (bmp1.getHeight() - bmp2.getHeight()) / 2;
        canvas.drawBitmap(bmp2, left, top, null);
        return bmOverlay;
    }

    /**
     * 图片大小定义
     */
    public static class PicSize {
        public int width;
        public int height;

        public PicSize() {
        }

        public PicSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
