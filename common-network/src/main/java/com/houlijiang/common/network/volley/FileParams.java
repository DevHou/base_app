package com.houlijiang.common.network.volley;

import android.text.TextUtils;
import android.util.Log;

import com.houlijiang.common.utils.IOUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by houlijiang on 14/11/20.
 * 
 * volley上传文件时使用的params，从async-http扒下来的代码
 */
public class FileParams {

    private static final String TAG = FileParams.class.getSimpleName();

    private final static String ENCODING = HTTP.UTF_8;
    public final static String APPLICATION_OCTET_STREAM = HTTP.OCTET_STREAM_TYPE;
    public final static String APPLICATION_JSON = "application/json";

    private static final String STR_CR_LF = "\r\n";
    private static final byte[] CR_LF = STR_CR_LF.getBytes();
    private static final byte[] TRANSFER_ENCODING_BINARY = ("Content-Transfer-Encoding: binary" + STR_CR_LF).getBytes();

    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        .toCharArray();

    public static final String HEADER_CONTENT_TYPE = HTTP.CONTENT_TYPE;
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String HEADER_CONTENT_ENCODING = HTTP.CONTENT_ENCODING;
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ENCODING_GZIP = "gzip";

    protected static final boolean autoCloseStream = true;
    protected ConcurrentHashMap<String, String> urlParams;
    protected ConcurrentHashMap<String, FileWrapper> fileParams;
    protected ConcurrentHashMap<String, StreamWrapper> streamParams;

    public FileParams() {
        init();
    }

    public FileParams(String key, String value) {
        init();
        put(key, value);
    }

    private void init() {
        urlParams = new ConcurrentHashMap<String, String>();
        fileParams = new ConcurrentHashMap<String, FileWrapper>();
        streamParams = new ConcurrentHashMap<String, StreamWrapper>();
    }

    /**
     * @param key key
     * @param value value
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    /**
     * @param key key
     * @param file 要上传的文件
     */
    public void put(String key, File file) {
        try {
            put(key, file, null, file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void put(String key, File file, String contentType, String customFileName) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
        if (key != null) {
            fileParams.put(key, new FileWrapper(file, contentType, customFileName));
        }
    }

    public void put(String key, InputStream stream) {
        put(key, stream, null, null);
    }

    /**
     * @param key key
     * @param stream 上传数据的stream
     * @param fileName 上传的文件名
     */
    public void put(String key, InputStream stream, String fileName) {
        put(key, stream, fileName, null);
    }

    /**
     * @param key key
     * @param stream 上传数据的stream
     * @param name 上传的文件名
     * @param contentType 上传文件的类型
     */
    public void put(String key, InputStream stream, String name, String contentType) {
        if (key != null && stream != null) {
            streamParams.put(key, StreamWrapper.newInstance(stream, name, contentType, autoCloseStream));
        }
    }

    /**
     * 获取entity，用于上传数据
     * 本应该在这里添加回调，反馈上传进度等
     */
    public HttpEntity getEntity() {
        HttpEntity entity = null;
        if (!fileParams.isEmpty() || !streamParams.isEmpty()) {
            MultipartEntity multipartEntity = new MultipartEntity();
            // Add string params
            for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
                multipartEntity.addPart(entry.getKey(), entry.getValue());
            }
            // Add stream params
            for (ConcurrentHashMap.Entry<String, StreamWrapper> entry : streamParams.entrySet()) {
                StreamWrapper stream = entry.getValue();
                if (stream.inputStream != null) {
                    multipartEntity.addPart(entry.getKey(), stream.name, stream.inputStream, stream.contentType);
                }
            }
            // Add file params
            for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
                FileWrapper fileWrapper = entry.getValue();
                multipartEntity.addPart(entry.getKey(), fileWrapper.file, fileWrapper.contentType,
                    fileWrapper.getFileName());
            }
            entity = multipartEntity;
        } else {
            try {
                entity = new UrlEncodedFormEntity(getParamsList(), ENCODING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

    protected List<BasicNameValuePair> getParamsList() {
        List<BasicNameValuePair> lparams = new ArrayList<BasicNameValuePair>();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return lparams;
    }

    private static class FileWrapper {
        public File file;
        public String fileName;
        public String contentType;

        public FileWrapper(File file, String fileName, String contentType) {
            this.file = file;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        public String getFileName() {
            if (fileName != null) {
                return fileName;
            } else {
                return "nofilename";
            }
        }
    }

    public static class StreamWrapper {
        public final InputStream inputStream;
        public final String name;
        public final String contentType;
        public final boolean autoClose;

        public StreamWrapper(InputStream inputStream, String name, String contentType, boolean autoClose) {
            this.inputStream = inputStream;
            this.name = name;
            this.contentType = contentType;
            this.autoClose = autoClose;
        }

        static StreamWrapper newInstance(InputStream inputStream, String name, String contentType, boolean autoClose) {
            return new StreamWrapper(inputStream, name, contentType == null ? APPLICATION_OCTET_STREAM : contentType,
                autoClose);
        }
    }

    class MultipartEntity implements HttpEntity {
        private final String boundary;
        private final byte[] boundaryLine;
        private final byte[] boundaryEnd;
        private final List<FilePart> fileParts = new ArrayList<FilePart>();

        private int bytesWritten;
        private int totalSize;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        public MultipartEntity() {
            final StringBuffer buf = new StringBuffer();
            final Random rand = new Random();
            for (int i = 0; i < 30; i++) {
                buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
            }
            this.boundary = buf.toString();
            this.boundaryLine = ("--" + boundary + STR_CR_LF).getBytes();
            this.boundaryEnd = ("--" + boundary + "--" + STR_CR_LF).getBytes();
        }

        private String normalizeContentType(String type) {
            return type == null ? APPLICATION_OCTET_STREAM : type;
        }

        private byte[] createContentType(String type) {
            String result = HEADER_CONTENT_TYPE + ": " + normalizeContentType(type) + STR_CR_LF;
            return result.getBytes();
        }

        private byte[] createContentDisposition(String key) {
            return (HEADER_CONTENT_DISPOSITION + ": form-data; name=\"" + key + "\"" + STR_CR_LF).getBytes();
        }

        private byte[] createContentDisposition(String key, String fileName) {
            return (HEADER_CONTENT_DISPOSITION + ": form-data; name=\"" + key + "\"" + "; filename=\"" + fileName
                + "\"" + STR_CR_LF).getBytes();
        }

        public void addPartWithCharset(String key, String value, String charset) {
            if (charset == null)
                charset = HTTP.UTF_8;
            addPart(key, value, "text/plain; charset=" + charset);
        }

        public void addPart(String key, String value) {
            addPartWithCharset(key, value, null);
        }

        public void addPart(final String key, final String value, String contentType) {
            try {
                out.write(boundaryLine);
                out.write(createContentDisposition(key));
                out.write(createContentType(contentType));
                out.write(CR_LF);
                out.write(value.getBytes());
                out.write(CR_LF);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        public void addPart(String key, File file) {
            addPart(key, file, null);
        }

        public void addPart(String key, File file, String type) {
            fileParts.add(new FilePart(key, file, normalizeContentType(type)));
        }

        public void addPart(String key, File file, String type, String customFileName) {
            fileParts.add(new FilePart(key, file, normalizeContentType(type), customFileName));
        }

        public void addPart(final String key, final String fileName, final InputStream fin, String type) {
            try {
                out.write(boundaryLine);

                // Headers
                out.write(createContentDisposition(key, fileName));
                out.write(createContentType(type));
                out.write(TRANSFER_ENCODING_BINARY);
                out.write(CR_LF);

                // Stream (file)
                final byte[] tmp = new byte[4096];
                int l;
                while ((l = fin.read(tmp)) != -1) {
                    out.write(tmp, 0, l);
                }

                out.write(CR_LF);
                out.flush();
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fin.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateProgress(int count) {
            bytesWritten += count;
            //httpResponse.onProgress(bytesWritten, totalSize);
        }

        private class FilePart {
            public File file;
            public byte[] header;

            public FilePart(String key, File file, String type, String customFileName) {
                header = createHeader(key, TextUtils.isEmpty(customFileName) ? file.getName() : customFileName, type);
                this.file = file;
            }

            public FilePart(String key, File file, String type) {
                header = createHeader(key, file.getName(), type);
                this.file = file;
            }

            private byte[] createHeader(String key, String filename, String type) {
                ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
                try {
                    headerStream.write(boundaryLine);

                    // Headers
                    headerStream.write(createContentDisposition(key, filename));
                    headerStream.write(createContentType(type));
                    headerStream.write(TRANSFER_ENCODING_BINARY);
                    headerStream.write(CR_LF);
                } catch (IOException e) {
                    // Can't happen on ByteArrayOutputStream
                    Log.e(TAG, "createHeader ByteArrayOutputStream exception", e);
                }
                return headerStream.toByteArray();
            }

            public long getTotalLength() {
                long streamLength = file.length() + CR_LF.length;
                return header.length + streamLength;
            }

            public void writeTo(OutputStream out) throws IOException {
                out.write(header);
                updateProgress(header.length);

                FileInputStream inputStream = new FileInputStream(file);
                final byte[] tmp = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(tmp)) != -1) {
                    out.write(tmp, 0, bytesRead);
                    updateProgress(bytesRead);
                }
                out.write(CR_LF);
                updateProgress(CR_LF.length);
                out.flush();
                IOUtils.closeSilently(inputStream);
            }
        }

        @Override
        public long getContentLength() {

            long contentLen = out.size();
            for (FilePart filePart : fileParts) {
                long len = filePart.getTotalLength();
                if (len < 0) {
                    return -1; // Should normally not happen
                }
                contentLen += len;
            }
            contentLen += boundaryEnd.length;
            return contentLen;
        }

        @Override
        public Header getContentType() {
            return new BasicHeader(HEADER_CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
        }

        @Override
        public boolean isChunked() {
            return false;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public boolean isStreaming() {
            return false;
        }

        @Override
        public void writeTo(final OutputStream outstream) throws IOException {
            bytesWritten = 0;
            totalSize = (int) getContentLength();
            out.writeTo(outstream);
            updateProgress(out.size());

            for (FilePart filePart : fileParts) {
                filePart.writeTo(outstream);
            }
            outstream.write(boundaryEnd);
            updateProgress(boundaryEnd.length);
        }

        @Override
        public Header getContentEncoding() {
            return null;
        }

        @Override
        public void consumeContent() throws IOException, UnsupportedOperationException {
            if (isStreaming()) {
                throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
            }
        }

        @Override
        public InputStream getContent() throws IOException, UnsupportedOperationException {
            throw new UnsupportedOperationException("getContent() is not supported. Use writeTo() instead.");
        }

    }
}
