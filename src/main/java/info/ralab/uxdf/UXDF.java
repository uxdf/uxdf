package info.ralab.uxdf;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * UXDF对象，包含SD定义和SD数据。
 *
 */
@Slf4j
@Data
public class UXDF {

    /**
     * UXDF内容默认字符集
     */
    public static final String CHARSET = StandardCharsets.UTF_8.name();
    /**
     * UXDF结构中sd内容的键
     */
    public static final String KEY_SD = "sd";
    /**
     * UXDF结构中data内容的键
     */
    public static final String KEY_DATA = "data";
    /**
     * UXDF文件默认后缀
     */
    public static final String FILE_EXTENSION = ".json";

    /**
     * SD定义
     */
    private Sd sd;
    /**
     * SD数据
     */
    private SdData data;
}
