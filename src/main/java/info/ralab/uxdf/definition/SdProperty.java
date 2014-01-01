package info.ralab.uxdf.definition;

import lombok.Data;

/**
 * SD属性定义
 */
@Data
public class SdProperty {
    /**
     * 属性标题
     */
    private String title;
    /**
     * 继承关系
     */
    private String[] extend;
    /**
     * 基本类型
     */
    private SdBaseType base;
    /**
     * 是否必填
     */
    private boolean required = false;
    /**
     * 默认值
     */
    private Object defaultValue;
    /**
     * 属性上限
     */
    private Object upperLimit;
    /**
     * 属性下限
     */
    private Object lowerLimit;
    /**
     * 取值范围
     */
    private Object valueSource;
    /**
     * 是否集合
     */
    private boolean isCollection;
    /**
     * 是否只读
     */
    private boolean readOnly = false;
    /**
     * 内容类型
     */
    private String contentType;
    /**
     * 校验规则
     */
    private SdPropertyValidRule[][] validRule;

}
