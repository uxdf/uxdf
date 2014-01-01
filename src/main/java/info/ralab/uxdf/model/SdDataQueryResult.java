package info.ralab.uxdf.model;

import info.ralab.uxdf.SdData;
import info.ralab.uxdf.UXDF;
import lombok.Data;

/**
 * {@link info.ralab.uxdf.SdData}查询结果
 */
@Data
public class SdDataQueryResult {


    /**
     * 查询返回的结果
     */
    private UXDF uxdf;

    /**
     * 承载分页或排序信息实例返回数量
     */
    private SdDataQuerySize mainSize;

    public SdDataQueryResult(final UXDF uxdf) {
        this.uxdf = uxdf;
        this.uxdf.setData(new SdData());
    }

}
