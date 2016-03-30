package com.common.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by houlijiang on 2014/9/19.
 *
 * 文件相关工具类
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    private static String mSDCardPath;

    /**
     * 获取挂在的sd card
     *
     * @return 所有sd card
     */
    private static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }

    /**
     * @param path 文件路径
     * @return 文件路径的StatFs对象
     */
    private static StatFs getStatFs(String path) {
        try {
            return new StatFs(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param stat 文件StatFs对象
     * @return 剩余存储空间的MB数
     */
    private static float calculateSizeInMB(StatFs stat) {
        if (stat != null)
            return stat.getAvailableBlocks() * (stat.getBlockSize() / (1024f * 1024f));
        return 0.0f;
    }

    /**
     * 尝试获取剩余容量大的sd card的路径
     *
     * @return 路径
     */
    private static String tryGetLargeSDCardPath() {
        HashSet<String> paths = getExternalMounts();
        if (paths == null || paths.size() == 0) {
            return Environment.getExternalStorageDirectory().getPath();
        }
        float sdcardSize = 0.0f;
        String avaliablePath = null;
        for (String path : paths) {
            float f = calculateSizeInMB(getStatFs(path));
            if (f > sdcardSize) {
                sdcardSize = f;
                avaliablePath = path;
            }
        }
        if (avaliablePath != null) {
            return avaliablePath;
        } else {
            return Environment.getExternalStorageDirectory().getPath();
        }

    }

    /**
     * 获取剩余容量大的sd card的路径，并检测是否可用
     *
     * @return 路径
     */
    public static String getLargeSDCardPath() {
        String path;
        if (!TextUtils.isEmpty(mSDCardPath)) {
            path = mSDCardPath;
        } else {
            path = tryGetLargeSDCardPath();
            mSDCardPath = path;
        }
        try {
            File file = new File(path, "temp");
            if (file.exists() && file.delete()) {
                return path;
            } else if (file.createNewFile()) {
                return path;

            }
        } catch (Exception e) {
            AppLog.e(TAG, "catch exception when check sdcard, e:" + e.getLocalizedMessage());
        }
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 在SDCARD中创建目录，并添加 nomedia 文件
     *
     * @return 创建的文件路径
     */
    public static File createDirIfNotExists(String path) {
        return createDirIfNotExists(path, true);
    }

    /**
     * 在SDCARD中创建目录
     * 
     * @param noMedia 是否创建nomedia文件
     *
     * @return 创建的文件路径
     */
    public static File createDirIfNotExists(String path, boolean noMedia) {
        AppLog.d(TAG, "check path:" + path);
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                AppLog.e(TAG, "Problem creating Image folder, path:" + file.getPath());
                return null;
            } else if (noMedia) {
                File mediaFile = new File(file.getPath() + "/.nomedia");
                try {
                    if (mediaFile.createNewFile()) {
                        return file;
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    AppLog.e(TAG, "create file error, e:" + e.getLocalizedMessage());
                    return null;
                }
            }
        }
        return file;
    }

    /**
     * 删除目录
     * 
     * @param path 目录
     * @return 是否成功
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * getFilesDir()方法用于获取/data/data/<application package>/files目录
     */
    public static String getInnerAppFilesDir(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    /**
     * getCacheDir()方法用于获取/data/data/<application package>/cache目录
     */
    public static String getInnerAppCacheDir(Context context) {
        return context.getCacheDir().getPath();
    }

    /**
     * 创建一个文件夹来检测文件是否有权限
     */
    private static boolean checkIfWritable(File file) {
        if (file == null) {
            return false;
        }
        String tempDir = "test_" + System.currentTimeMillis();
        File temp = new File(file, tempDir);
        if (temp.mkdirs()) {
            deleteDirectory(temp);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取cache目录，如果有SDCard并且SDCard不可卸载并且有写权限就使用SDCard，否则使用内部存储
     *
     * getExternalCacheDir()方法可以获取到 SDCard/Android/data/你的应用包名/cache/目录
     */
    public static String tryGetGoodDiskCacheDir(Context context) {
        String cachePath;
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && !Environment.isExternalStorageRemovable() && context.getExternalCacheDir() != null) {
                File f = context.getExternalCacheDir();
                if (checkIfWritable(f)) {
                    cachePath = f.getAbsolutePath();
                } else {
                    cachePath = context.getCacheDir().getAbsolutePath();
                }
            } else {
                cachePath = context.getCacheDir().getAbsolutePath();
            }
        } catch (Exception e) {
            AppLog.e(TAG, "get good cache dir error, e:" + e.getLocalizedMessage());
            cachePath = context.getCacheDir().getAbsolutePath();
        }
        return cachePath;
    }

    /**
     * 获取cache目录，如果有SDCard并且挂载了就使用SDCard否则使用内部存储
     * 
     * getExternalCacheDir()方法可以获取到 SDCard/Android/data/你的应用包名/cache/目录
     */
    public static String tryGetBadDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
            && context.getExternalCacheDir() != null) {
            File f = context.getExternalCacheDir();
            if (checkIfWritable(f)) {
                cachePath = f.getAbsolutePath();
            } else {
                cachePath = context.getCacheDir().getAbsolutePath();
            }
        } else {
            cachePath = context.getCacheDir().getAbsolutePath();
        }
        return cachePath;
    }

    /**
     * 获取files目录，如果有sd card并且sd card不可卸载并且有写权限就使用SDCard，否则使用内部存储
     *
     * getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/
     */
    public static String tryGetGoodDiskFilesDir(Context context) {
        String filePath;
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && !Environment.isExternalStorageRemovable()) {
                File f = context.getExternalFilesDir(null);
                if (f != null && checkIfWritable(f)) {
                    filePath = f.getAbsolutePath();
                } else {
                    filePath = context.getFilesDir().getAbsolutePath();
                }
            } else {
                filePath = context.getFilesDir().getAbsolutePath();
            }
        } catch (Exception e) {
            AppLog.e(TAG, "get good file dir error, e:" + e.getLocalizedMessage());
            filePath = context.getFilesDir().getAbsolutePath();
        }
        return filePath;
    }

    /**
     * 获取files目录，如果有SDCard并且挂载了就使用SDCard否则使用内部存储
     *
     * getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/
     */
    public static String tryGetBadDiskFilesDir(Context context) {
        String filePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File f = context.getExternalFilesDir(null);
            if (f != null && checkIfWritable(f)) {
                filePath = f.getAbsolutePath();
            } else {
                filePath = context.getFilesDir().getAbsolutePath();
            }
        } else {
            filePath = context.getFilesDir().getAbsolutePath();
        }
        return filePath;
    }

    /**
     * 检测Sdcard是否存在
     *
     * @return 是否存在
     */
    public static boolean isExitsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 读取指定文件的输出
     */
    public static String getFileOutputString(String path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path), 8192);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append("\n").append(line);
            }
            bufferedReader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
