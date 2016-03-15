package com.common.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by houlijiang on 14-10-15.
 * 
 * 字符串处理工具类
 */
public class StringUtils {

    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
        "e", "f" };

    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String toMD5(String origin) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return byteArrayToHexString(md.digest(origin.getBytes()));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String encodeURL(String url, String encode) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        StringBuilder noAsciiPart = new StringBuilder();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c > 255) {
                noAsciiPart.append(c);
            } else {
                if (noAsciiPart.length() != 0) {
                    sb.append(URLEncoder.encode(noAsciiPart.toString(), encode));
                    noAsciiPart.delete(0, noAsciiPart.length());
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static int string2Int(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }

        int ret = 0;
        try {
            ret = Integer.parseInt(str);
        } catch (Exception e) {

        }
        return ret;
    }

    public static long parseLong(String str, long defaultValue) {
        if (isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isBlank(String str) {
        return str != null && isEmpty(str.trim());
    }

    public static boolean isNull(String str) {
        return str == null || "null".equalsIgnoreCase(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str) && !isNull(str);
    }

    public static String toString(int value) {
        return String.valueOf(value);
    }

    public static String toString(boolean value) {
        return String.valueOf(value);
    }

    public static String toString(double value) {
        return String.valueOf(value);
    }

    public static String toString(long value) {
        return String.valueOf(value);
    }

    // /**
    // * 返回前大后小样式的字符串
    // * @param content 文字内容
    // * @param size 字号
    // * @return
    // */
    // public static SpannableString scoreSpannable(String content, int size) {
    // int large = size;
    // int small = large -
    // AppContext.getInstance().getResources().getDimensionPixelSize(R.dimen.px2);
    // if (small <= 0) {
    // small = 2;
    // }
    // return scoreSpannable(content, large, small);
    // }

    /**
     * 返回前大后小样式的字符串
     *
     * @param content
     *            文字内容
     * @param large
     *            字号(大)
     * @param small
     *            字号(小)
     * @return
     */
    public static SpannableString scoreSpannable(String content, int large, int small) {
        SpannableString spannableString = new SpannableString(content);
        if (large <= 0 || small <= 0) {
            return spannableString;
        }
        int index = content.indexOf(".");
        int end = index > -1 ? index : 1;
        spannableString.setSpan(new AbsoluteSizeSpan(large), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(small), end, spannableString.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /**
     * encoded in utf-8
     *
     * <pre>
     * utf8Encode(null) = null
     * utf8Encode("") = "";
     * utf8Encode("aa") = "aa";
     * utf8Encode("啊啊啊啊") = "%E5%95%8A%E5%95%8A%E5%95%8A%E5%95%8A";
     * </pre>
     *
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     *             if an error occurs
     */
    public static String utf8Encode(String str) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
            }
        }
        return str;
    }

    /**
     * get innerHtml from href
     *
     * <pre>
     * getHrefInnerHtml(null) = ""
     * getHrefInnerHtml("") = ""
     * getHrefInnerHtml("mp3") = "mp3";
     * getHrefInnerHtml("&lt;a innerHtml&lt;/a&gt;") = "&lt;a innerHtml&lt;/a&gt;";
     * getHrefInnerHtml("&lt;a&gt;innerHtml&lt;/a&gt;") = "innerHtml";
     * getHrefInnerHtml("&lt;a&lt;a&gt;innerHtml&lt;/a&gt;") = "innerHtml";
     * getHrefInnerHtml("&lt;a href="baidu.com"&gt;innerHtml&lt;/a&gt;") = "innerHtml";
     * getHrefInnerHtml("&lt;a href="baidu.com" title="baidu"&gt;innerHtml&lt;/a&gt;") = "innerHtml";
     * getHrefInnerHtml("   &lt;a&gt;innerHtml&lt;/a&gt;  ") = "innerHtml";
     * getHrefInnerHtml("&lt;a&gt;innerHtml&lt;/a&gt;&lt;/a&gt;") = "innerHtml";
     * getHrefInnerHtml("jack&lt;a&gt;innerHtml&lt;/a&gt;&lt;/a&gt;") = "innerHtml";
     * getHrefInnerHtml("&lt;a&gt;innerHtml1&lt;/a&gt;&lt;a&gt;innerHtml2&lt;/a&gt;") = "innerHtml2";
     * </pre>
     *
     * @param href
     * @return <ul>
     *         <li>if href is null, return ""</li>
     *         <li>if not match regx, return source</li>
     *         <li>return the last string that match regx</li>
     *         </ul>
     */
    public static String getHrefInnerHtml(String href) {
        if (isEmpty(href)) {
            return "";
        }

        String hrefReg = ".*<[\\s]*a[\\s]*.*>(.+?)<[\\s]*/a[\\s]*>.*";
        Pattern hrefPattern = Pattern.compile(hrefReg, Pattern.CASE_INSENSITIVE);
        Matcher hrefMatcher = hrefPattern.matcher(href);
        if (hrefMatcher.matches()) {
            return hrefMatcher.group(1);
        }
        return href;
    }

    /**
     * process special char in html
     *
     * <pre>
     * htmlEscapeCharsToString(null) = null;
     * htmlEscapeCharsToString("") = "";
     * htmlEscapeCharsToString("mp3") = "mp3";
     * htmlEscapeCharsToString("mp3&lt;") = "mp3<";
     * htmlEscapeCharsToString("mp3&gt;") = "mp3\>";
     * htmlEscapeCharsToString("mp3&amp;mp4") = "mp3&mp4";
     * htmlEscapeCharsToString("mp3&quot;mp4") = "mp3\"mp4";
     * htmlEscapeCharsToString("mp3&lt;&gt;&amp;&quot;mp4") = "mp3\<\>&\"mp4";
     * </pre>
     *
     * @param source
     * @return
     */
    public static String htmlEscapeCharsToString(String source) {
        return StringUtils.isEmpty(source) ? source : source.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
            .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
    }

    /**
     * 全角转半角
     *
     * <pre>
     * fullWidthToHalfWidth(null) = null;
     * fullWidthToHalfWidth("") = "";
     * fullWidthToHalfWidth(new String(new char[] {12288})) = " ";
     * fullWidthToHalfWidth("！＂＃＄％＆) = "!\"#$%&";
     * </pre>
     *
     */
    public static String fullWidthToHalfWidth(String s) {
        if (isEmpty(s)) {
            return s;
        }

        char[] source = s.toCharArray();
        for (int i = 0; i < source.length; i++) {
            if (source[i] == 12288) {
                source[i] = ' ';
                // } else if (source[i] == 12290) {
                // source[i] = '.';
            } else if (source[i] >= 65281 && source[i] <= 65374) {
                source[i] = (char) (source[i] - 65248);
            } else {
                source[i] = source[i];
            }
        }
        return new String(source);
    }

    /**
     * 半角转全角
     *
     * <pre>
     * halfWidthToFullWidth(null) = null;
     * halfWidthToFullWidth("") = "";
     * halfWidthToFullWidth(" ") = new String(new char[] {12288});
     * halfWidthToFullWidth("!\"#$%&) = "！＂＃＄％＆";
     * </pre>
     *
     */
    public static String halfWidthToFullWidth(String s) {
        if (isEmpty(s)) {
            return s;
        }

        char[] source = s.toCharArray();
        for (int i = 0; i < source.length; i++) {
            if (source[i] == ' ') {
                source[i] = (char) 12288;
                // } else if (source[i] == '.') {
                // source[i] = (char)12290;
            } else if (source[i] >= 33 && source[i] <= 126) {
                source[i] = (char) (source[i] + 65248);
            } else {
                source[i] = source[i];
            }
        }
        return new String(source);
    }

    /**
     * 获取字符串长度
     * 汉字算2个
     */
    public static int getStringLen(String origin) {
        int len = 0;
        for (int i = 0; i < origin.length(); i++) {
            Character c = origin.charAt(i);
            if (c.toString().getBytes().length != 1) {
                len += 2;
            } else {
                len += 1;
            }
        }
        return len;
    }

    /**
     * 获取混合字符串中部分字符串，英文字母算1个，其他算2个
     * 
     * @param sub 默认后面加的后缀
     */
    public static String getMixSubString(String origin, int max, String sub) {
        String result = "";
        if (TextUtils.isEmpty(origin) || max <= 0) {
            return result;
        }
        int len = 0;
        int i;
        for (i = 0; i < origin.length(); i++) {
            Character c = origin.charAt(i);
            if (c.toString().getBytes().length != 1) {
                len += 2;
            } else {
                len += 1;
            }
            result += c;
            if (len >= max) {
                break;
            }
        }
        if (i < origin.length()) {
            result += sub;
        }
        return result;
    }

    /**
     * 获取混合字符串中部分字符串，英文字母算1个，其他算2个
     */
    public static String getMixSubString(String origin, int max) {
        return getMixSubString(origin, max, "");
    }

    /**
     * 获取某种分隔符最后的子串
     * 
     * @param org 原始字符串
     * @param split 分隔符
     * @return 子串
     */
    public static String getLastSplit(String org, String split) {
        if (TextUtils.isEmpty(org)) {
            return "";
        }
        return org.substring(org.lastIndexOf(split) + 1, org.length());
    }

    /**
     * 判断电话号码是否正确
     */
    public static boolean isMobileNumber(String mobiles) {
        Pattern p =
            Pattern
                .compile("((\\d{11})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$)");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 判断email格式是否正确
     */
    public static boolean isEmail(String email) {
        String str =
            "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 判断是否全是数字
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
}
