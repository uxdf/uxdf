package info.ralab.uxdf.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

/**
 * UXDF定义文件信息
 */

@Data
@AllArgsConstructor
public class UXDFFileInfo {
    /**
     * 文件名称
     */
    private String name;
    /**
     * 文件输入流
     */
    private InputStream inputStream;
    /**
     * 文件路径
     */
    private String path;
}
