package info.ralab.uxdf.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link info.ralab.uxdf.SdData}查询排序条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdDataQueryOrder {

    /**
     * 排序的属性名称
     */
    private String property;

    /**
     * 排序类型
     */
    private SdDataQueryOrderType type;

    /**
     * 获取一个升序排序条件
     *
     * @param property 排序的属性名称
     * @return 升序排序对象
     */
    public static SdDataQueryOrder asc(final String property) {
        return new SdDataQueryOrder(property, SdDataQueryOrderType.ASC);
    }

    /**
     * 获取一个降序排序条件
     *
     * @param property 排序的属性名称
     * @return 降序排序对象
     */
    public static SdDataQueryOrder desc(final String property) {
        return new SdDataQueryOrder(property, SdDataQueryOrderType.DESC);
    }
}
