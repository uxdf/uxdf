package info.ralab.uxdf.definition;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * SD Node定义
 */
@Data
public class SdNode {

    /**
     * 基础属性
     */
    private Map<String, SdProperty> attr = Maps.newLinkedHashMap();

    /**
     * 扩展属性
     */
    private Map<String, SdNodeDefinition> impl = Maps.newLinkedHashMap();

    /**
     * 设置Sd Node的自扩展属性{@link SdNodeDefinition}。
     * 此方法会清空已有的自扩展属性。
     *
     * @param impl Sd Node扩展属性集合
     */
    public void setImpl(Map<String, SdNodeDefinition> impl) {
        this.impl.clear();
        if (impl == null || impl.isEmpty()) {
            return;
        }
        impl.forEach((nodeName, nodeDefinition) -> {
            nodeDefinition.setNodeName(nodeName);
            this.impl.put(nodeName, nodeDefinition);
        });
    }

    /**
     * 合并另一个Sd Node定义{@link SdNode}中的扩展属性到当前Sd Node。<br />
     * 合并中如果出现同名扩展属性，会使用来源SdNode中的扩展属性{@link SdNodeDefinition}覆盖当前的。
     *
     * @param node 要合并的来源Sd Node
     */
    public void merge(final SdNode node) {
        if (node == null || node.getImpl() == null) {
            return;
        }
        this.impl.putAll(node.getImpl());
    }
}
