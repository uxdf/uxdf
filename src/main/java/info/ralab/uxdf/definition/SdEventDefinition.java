package info.ralab.uxdf.definition;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.ralab.uxdf.instance.EventEntity;
import info.ralab.uxdf.instance.SdEntity;
import lombok.Data;

import java.util.Map;
import java.util.Set;

import static info.ralab.uxdf.definition.SdEventRequiredType.*;

@Data
public class SdEventDefinition extends SdDefinition {


    private final static Set<String> INDEX_SET = Sets.newHashSet(SdEntity.ATTR_ID, EventEntity.ATTR_LEFT, EventEntity.ATTR_RIGHT);

    private Boolean isMember = false;

    private Boolean isLeftMaster = false;

    private Boolean isRightMaster = false;

    private SdEventRequiredType required = none;

    private boolean readonly = false;

    private Map<String, SdProperty> prop = Maps.newHashMap();


    @JSONField(serialize = false)
    private String leftNodeName;
    @JSONField(serialize = false)
    private String rightNodeName;
    @JSONField(serialize = false)
    private String eventName;

    /**
     * 获取目标Node名称
     *
     * @return
     */
    public String getTargetNodeName(final String nodeName) {
        return nodeName.equals(leftNodeName) ? rightNodeName : leftNodeName;
    }

    /**
     * 判断Event对于Node是否必填
     *
     * @param nodeName
     * @return
     */
    public boolean isRequired(final String nodeName) {
        return this.leftNodeName.equals(nodeName) && (this.required == left || this.required == both) ||
                this.rightNodeName.equals(nodeName) && (this.required == right || this.required == both);
    }

    /**
     * 判断Node名称在当前{@link SdEventDefinition}中是否是成员
     *
     * @param nodeName Node名称
     * @return 是否是成员
     */
    public boolean isMember(final String nodeName) {
        return this.isMember && this.leftNodeName.equals(nodeName);
    }

    /**
     * 判断Node是否是Master
     *
     * @param nodeName Node名称
     * @return 是否是主
     */
    public boolean isMaster(final String nodeName) {
        return this.leftNodeName.equals(nodeName) && this.isLeftMaster ||
                this.rightNodeName.equals(nodeName) && this.isRightMaster;
    }


    @Override
    public int hashCode() {
        return (this.getLeftNodeName() + this.getEventName() + this.getRightNodeName()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SdEventDefinition && obj.hashCode() == this.hashCode();
    }

    /**
     * 判断属性是否索引
     *
     * @param property 属性
     * @return 是否索引
     */
    @Override
    public boolean isIndex(String property) {
        return INDEX_SET.contains(property);
    }
}
