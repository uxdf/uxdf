package info.ralab.uxdf.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static info.ralab.uxdf.model.SdDataQueryLogic.*;

/**
 * {@link info.ralab.uxdf.SdData}查询参数
 */
@Data
@NoArgsConstructor
public class SdDataQueryParam {
    /**
     * 创建属性等于比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam equal(final String property, final Object value) {
        return new SdDataQueryParam(property, value, EQ);
    }

    /**
     * 创建属性不等于比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam notEqual(final String property, final Object value) {
        return new SdDataQueryParam(property, value, NE);
    }

    /**
     * 创建属性大于比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam greater(final String property, final Object value) {
        return new SdDataQueryParam(property, value, GT);
    }

    /**
     * 创建属性小于比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam less(final String property, final Object value) {
        return new SdDataQueryParam(property, value, LT);
    }

    /**
     * 创建属性大于等于比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam greaterAndEqual(final String property, final Object value) {
        return new SdDataQueryParam(property, value, GTE);
    }

    /**
     * 创建属性小于等于比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam lessAndEqual(final String property, final Object value) {
        return new SdDataQueryParam(property, value, LTE);
    }

    /**
     * 创建属性相似比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam like(final String property, final Object value) {
        return new SdDataQueryParam(property, value, LIKE);
    }

    /**
     * 创建属性起始一致比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam startWith(final String property, final Object value) {
        return new SdDataQueryParam(property, value, SW);
    }

    /**
     * 创建属性结尾一致比较值的逻辑条件
     *
     * @param property 属性
     * @param value    比较值
     * @return 逻辑条件
     */
    public static SdDataQueryParam endWith(final String property, final Object value) {
        return new SdDataQueryParam(property, value, EW);
    }

    /**
     * 创建属性为空的逻辑条件
     *
     * @param property 属性
     * @return 逻辑条件
     */
    public static SdDataQueryParam isNull(final String property) {
        return new SdDataQueryParam(property, null, NULL);
    }

    /**
     * 创建属性不为空的逻辑条件
     *
     * @param property 属性
     * @return 逻辑条件
     */
    public static SdDataQueryParam notNull(final String property) {
        return new SdDataQueryParam(property, null, NN);
    }

    /**
     * 创建属性存在的逻辑条件
     *
     * @param property 属性
     * @return 逻辑条件
     */
    public static SdDataQueryParam exist(final String property) {
        return new SdDataQueryParam(property, null, EXIST);
    }

    /**
     * 创建属性不存在的逻辑条件
     *
     * @param property 属性
     * @return 逻辑条件
     */
    public static SdDataQueryParam notExist(final String property) {
        return new SdDataQueryParam(property, null, NEX);
    }

    /**
     * 构造函数
     *
     * @param property 属性
     * @param value    比较值
     * @param logic    逻辑类型
     */
    public SdDataQueryParam(final String property, final Object value, final SdDataQueryLogic logic) {
        this.property = property;
        this.value = value;
        this.logic = logic;
    }

    /**
     * 逻辑条件比较的属性
     */
    @NonNull
    private String property;

    /**
     * 逻辑条件比较的值
     */
    private Object value;

    /**
     * 逻辑条件比较的逻辑类型，默认为相等
     */
    private SdDataQueryLogic logic = EQ;
}
