package com.common.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by houlijiang on 2014/9/20.
 *
 * stream相关工具类
 */
public class IOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 KB

    public static boolean copyStream(InputStream is, OutputStream os) throws IOException {
        return copyStream(is, os, DEFAULT_BUFFER_SIZE);
    }

    public static boolean copyStream(InputStream is, OutputStream os, int bufferSize)
            throws IOException {

        final byte[] bytes = new byte[bufferSize];
        int count;
        while ((count = is.read(bytes, 0, bufferSize)) != -1) {
            os.write(bytes, 0, count);
        }
        os.flush();
        return true;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
