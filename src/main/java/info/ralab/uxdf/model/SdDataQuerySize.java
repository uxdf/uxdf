package info.ralab.uxdf.model;

import lombok.Data;

/**
 * 承载分页或排序信息实例返回数量
 */
@Data
public class SdDataQuerySize {

    /**
     * 本次查询范围主Sd总的数量
     */
    private long count = 0;

    /**
     * 本次查询范围主Sd返回的数量
     */
    private long current = 0;
}
