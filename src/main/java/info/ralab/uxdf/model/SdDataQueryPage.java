package info.ralab.uxdf.model;

import lombok.Data;

/**
 * 数据查询分页参数，设定分页跳过记录数和单页返回记录数。
 */
@Data
public class SdDataQueryPage {

    public SdDataQueryPage() {
    }

    public SdDataQueryPage(int start) {
        this.start = start;
    }

    public SdDataQueryPage(int start, int limit) {
        this.start = start;
        this.limit = limit;
    }

    /**
     * 默认单页记录数
     */
    public static final int DEFAULT_LIMIT = 10;

    /**
     * 分页跳过记录数
     */
    private int start;

    /**
     * 单页返回记录数
     */
    private int limit = DEFAULT_LIMIT;
}
