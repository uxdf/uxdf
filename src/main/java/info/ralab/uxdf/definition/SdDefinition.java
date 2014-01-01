package info.ralab.uxdf.definition;

import lombok.Data;

import java.util.Arrays;
import java.util.Map;

/**
 * Sd基本定义
 */
@Data
public abstract class SdDefinition {
    /**
     * Sd标题
     */
    private String title;
    /**
     * Sd展示属性
     */
    private String[] display;
    /**
     * Sd属于的命名空间
     */
    private String[] namespace;
    /**
     * Sd继承关系。继承顺序从左至右，子覆盖父的属性。
     */
    private String[] extend;
    /**
     * 唯一属性
     */
    private String[] uniqueIndex;

    /**
     * 获取自定属性{@link SdProperty}集合
     *
     * @return 自定义属性集合
     */
    public abstract Map<String, SdProperty> getProp();

    /**
     * 检查属性在当前Sd中是否作为索引使用
     *
     * @param propertyName 属性名称
     * @return 是否索引
     */
    public abstract boolean isIndex(final String propertyName);

    /**
     * 设置唯一索引属性集合，并进行排序
     *
     * @param uniqueIndex 唯一索引属性集合
     */
    public void setUniqueIndex(final String[] uniqueIndex) {
        this.uniqueIndex = uniqueIndex;
        if (this.uniqueIndex != null && this.uniqueIndex.length > 0) {
            Arrays.sort(this.uniqueIndex);
        }
    }

    /**
     * 当前Sd定义是否属于某个命名空间
     *
     * @param namespace 命名空间
     * @return 是否属于某个命名空间
     */
    public boolean isNamespace(final String namespace) {
        if (this.namespace == null || this.namespace.length < 1) {
            return false;
        }
        return Arrays.binarySearch(this.namespace, namespace) > -1;
    }

    /**
     * 设置当前Sd定义属于的命名空间定义，并进行排序
     *
     * @param namespace 命名控件集合
     */
    public void setNamespace(final String[] namespace) {
        this.namespace = namespace;
        if (this.namespace != null && this.namespace.length > 0) {
            Arrays.sort(this.namespace);
        }
    }
}
