package com.houlijiang.common.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by houlijiang on 2014/6/5.
 * 
 * 解zip包的工具类
 */
public class ZipUtils {
    private static final String TAG = ZipUtils.class.getSimpleName();

    private String _zipFile;
    private String _location;

    /**
     * 解压
     * 
     * @param zipFile zip文件路径
     * @param location 输出路径
     */
    public ZipUtils(String zipFile, String location) {
        _zipFile = zipFile;
        _location = location;
        _dirChecker("");
    }

    public void unzip() {
        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.d(TAG, "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    _dirChecker(ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(_location + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }
                    zin.closeEntry();
                    fout.close();
                }
            }
            zin.close();
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
        }
    }

    private void _dirChecker(String dir) {
        File f = new File(_location + "/" + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
