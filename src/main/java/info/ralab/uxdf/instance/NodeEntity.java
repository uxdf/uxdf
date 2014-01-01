package info.ralab.uxdf.instance;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.ralab.uxdf.UXDFException;
import info.ralab.uxdf.UXDFLoader;
import info.ralab.uxdf.chain.UXDFChain;
import info.ralab.uxdf.definition.SdEventDefinition;
import info.ralab.uxdf.definition.SdNodeDefinition;
import info.ralab.uxdf.utils.AssociateUniquePropertyUtil;
import info.ralab.uxdf.utils.UXDFHelper;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeEntity extends SdEntity {

    /**
     * 不支持合并的属性
     */
    public final static Set<String> UNSUPPORTED_MERGE_PROPS = Sets.newHashSet(
            ATTR_ID,
            ATTR_SD,
            ATTR_CREATE_TIME,
            ATTR_UPDATE_TIME,

            DYNA_OPERATE,
            DYNA_OPERATE_DELETE_ENFORCE,
            DYNA_AUTHORITY,
            DYNA_SYNC_LOCK,
            DYNA_OPERATE_CREATE_ORIGINAL_ID
    );

    @Getter
    @JSONField(serialize = false)
    private Map<SdEventDefinition, List<NodeEntity>> children = Maps.newHashMap();

    public NodeEntity() {
        super();
    }

    public NodeEntity(final JSONObject json) {
        super();
        this.putAll(json);
    }

    public NodeEntity(final String sd, final String id) {
        super(sd, id);
    }

    /**
     * 获取实体展示内容
     *
     * @return 展示内容
     */
    @Override
    public String getEntityDisplay() {
        SdNodeDefinition sdNode = UXDFLoader.getNode(this.get__Sd());
        return makeEntityDisplay(sdNode);
    }

    @Override
    public NodeEntity id(final String id) {
        this.set__Id(id);
        return this;
    }

    @Override
    public NodeEntity sd(String sd) {
        this.set__Sd(sd);
        return this;
    }

    @Override
    public StringBuilder makeBaseProperties() {
        return super.makeBaseProperties();
    }

    @Override
    public boolean isEffective() {
        return super.isEffective();
    }

    @Override
    public NodeEntity merge(SdEntity sdEntity) {
        sdEntity.forEach((propKey, propValue) -> {
            // 跳过不合并的属性
            if (UNSUPPORTED_MERGE_PROPS.contains(propKey)) {
                return;
            }
            this.put(propKey, propValue);
        });
        return this;
    }

    public void putChild(final SdEventDefinition sdEvent, final NodeEntity child) {
        List<NodeEntity> nodeEntities;
        if (this.children.containsKey(sdEvent)) {
            nodeEntities = this.children.get(sdEvent);
        } else {
            nodeEntities = Lists.newArrayList();
            this.children.put(sdEvent, nodeEntities);
        }

        nodeEntities.add(child);
    }

    /**
     * 生成唯一ID
     *
     * @return 唯一ID
     */
    @Override
    public String generateUUID() {
        if (!this.isEffective()) {
            throw new UXDFException(
                    String.format(
                            "[%s]不是一个有效的Node。",
                            this.toJSONString()
                    )
            );
        }
        SdNodeDefinition sdNode = UXDFLoader.getNode(this.get__Sd());
        StringBuilder uuidBuilder = this.makeBaseProperties();
        if (sdNode == null || sdNode.getUniqueIndex() == null || sdNode.getUniqueIndex().length == 0) {
            // 无唯一属性，默认唯一属性为版本信息和__id
            uuidBuilder.append(this.get__Id());
        } else {
            for (String index : sdNode.getUniqueIndex()) {

                Object value = this.get(index);
                if (value == null) {
                    // 唯一属性不完整，不能生成UUID
                    if (UXDFChain.haveRelationship(index)) {
                        // 唯一属性有关系表达式，获取关联唯一属性值
                        final String uniqueProperty = AssociateUniquePropertyUtil.getPropertyName(sdNode);
                        // 无法获取属性名
                        if (uniqueProperty == null) {
                            return null;
                        }
                        value = this.get(uniqueProperty);
                        // 关联唯一属性无法获取
                        if (value == null) {
                            return null;
                        }
                        uuidBuilder.append(value);
                    } else {
                        // 唯一属性没有关系表达式
                        return null;
                    }
                } else {
                    uuidBuilder.append(value);
                }
            }
        }
        String uuid = UXDFHelper.generate(uuidBuilder.toString());
        this.set__Uuid(uuid);
        return uuid;
    }

    /**
     * 获取唯一ID，如果当前已经计算过直接使用，如果没有计算过，重新计算获取
     *
     * @return 唯一ID
     */
    @Override
    public String getUUID() {
        if (this.get__Uuid() == null || this.get__Uuid().isEmpty()) {
            this.set__Uuid(generateUUID());
        }
        return this.get__Uuid();
    }

    @Override
    public String getLogicId() {
        return makeBaseProperties().append(this.get__Id()).toString();
    }

    @Override
    public int hashCode() {
        return getLogicId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NodeEntity && getLogicId().equals(((NodeEntity) obj).getLogicId());
    }

    @Override
    public NodeEntity clone() {
        NodeEntity cloneNode = new NodeEntity(this);
        // 移除uuid，防止唯一属性变更后，uuid发生错误
        cloneNode.remove(SdEntity.ATTR_UUID);
        return cloneNode;
    }

}
