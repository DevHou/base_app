package com.common.image;

/**
 * Created by houlijiang on 14/11/21.
 *
 * 图片处理错误码
 */
public class ImageLoadError {

    public static final int ERROR_NETWORK = 1;
    public static final int ERROR_SERVER = 2;
    public static final int ERROR_DECODE = 3;
    public static final int ERROR_MEMORY = 4;
    public static final int ERROR_NONE_URI = 5;
    public static final int ERROR_UNKNOWN = 6;

    private int code;
    private String reason;

    public ImageLoadError(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
