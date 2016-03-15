package com.common.network;

import java.io.File;

/**
 * Created by houlijiang on 14/11/19.
 *
 * 上传的文件的包装
 */
public class FileWrapper {
    public final File file;
    public final String contentType;
    public String customFileName;

    public FileWrapper(File file, String contentType, String customFileName) {
        this.file = file;
        this.contentType = contentType;
        this.customFileName = customFileName;
    }
}
