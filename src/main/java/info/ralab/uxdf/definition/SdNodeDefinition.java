package info.ralab.uxdf.definition;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Sets;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * SD Node定义
 */
@Data
public class SdNodeDefinition extends SdDefinition {

    // 需要单独创建索引的属性
    private final static Set<String> INDEX_SET = Sets.newHashSet();

    @JSONField(name = "buildProcess")
    private Object buildProcessDetail;

    private LinkedHashMap<String, SdProperty> prop;

    @JSONField(deserialize = false)
    private String nodeName;

    @JSONField(serialize = false)
    private String buildProcess;


    @Override
    public int hashCode() {
        return this.getNodeName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SdNodeDefinition && obj.hashCode() == this.hashCode();
    }

    /**
     * 检查属性在当前Node中是否作为索引使用
     *
     * @param propertyName 属性名称
     * @return 是否索引
     */
    @Override
    public boolean isIndex(final String propertyName) {
        return INDEX_SET.contains(propertyName);
    }
}
