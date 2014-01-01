package info.ralab.uxdf.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * 二进制文件信息
 */
public interface UXDFBinaryFileInfo {

    String PROP_NAME = "name";
    String PROP_CONTENT_TYPE = "contentType";
    String PROP_LENGTH = "length";
    int HEAD_LENGTH = 1024;

    /**
     * 默认文件内容类型
     */
    String CONTEXT_TYPE = "application/octet-stream";

    /**
     * 文件名
     *
     * @return 文件名
     */
    String getName();

    /**
     * 文件内容类型
     *
     * @return 内容类型
     */
    String getContentType();

    /**
     * 文件大小
     *
     * @return 文件大小
     */
    long getLength();

    /**
     * 文件输入流
     *
     * @return 输入流
     * @throws IOException IO异常
     */
    InputStream getInputStream() throws IOException;

    /**
     * 是否是文件
     *
     * @return 是否是文件
     */
    boolean isFile();
}
