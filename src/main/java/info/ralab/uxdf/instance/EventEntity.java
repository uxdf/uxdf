package info.ralab.uxdf.instance;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import info.ralab.uxdf.UXDFException;
import info.ralab.uxdf.UXDFLoader;
import info.ralab.uxdf.definition.SdEventDefinition;
import info.ralab.uxdf.utils.UXDFHelper;

import java.util.Set;

public class EventEntity extends SdEntity {

    /**
     * 左节点ID
     */
    public final static String ATTR_LEFT = "__left";
    /**
     * 左节点SD名称
     */
    public final static String ATTR_LEFT_SD = "__leftSd";
    /**
     * 右节点ID
     */
    public final static String ATTR_RIGHT = "__right";
    /**
     * 右节点SD名称
     */
    public final static String ATTR_RIGHT_SD = "__rightSd";
    /**
     * 左节点是否是右节点的成员
     */
    public final static String ATTR_IS_MEMBER = "__isMember";

    /**
     * 不支持合并的属性
     */
    public final static Set<String> UNSUPPORTED_MERGE_PROPS = Sets.newHashSet(
            ATTR_ID,
            ATTR_SD,
            ATTR_CREATE_TIME,
            ATTR_UPDATE_TIME,
            ATTR_LEFT,
            ATTR_LEFT_SD,
            ATTR_RIGHT,
            ATTR_RIGHT_SD,
            ATTR_IS_MEMBER,

            DYNA_OPERATE,
            DYNA_OPERATE_DELETE_ENFORCE,
            DYNA_AUTHORITY,
            DYNA_OPERATE_CREATE_ORIGINAL_ID
    );

    public EventEntity() {
        super();
    }

    public EventEntity(final JSONObject json) {
        super();
        this.putAll(json);
    }

    public String get__Left() {
        return this.getString(ATTR_LEFT);
    }

    public void set__Left(final String left) {
        this.put(ATTR_LEFT, left);
    }

    public String get__LeftSd() {
        return this.getString(ATTR_LEFT_SD);
    }

    public void set__LeftSd(final String leftSd) {
        this.put(ATTR_LEFT_SD, leftSd);
    }


    public String get__Right() {
        return this.getString(ATTR_RIGHT);
    }

    public void set__Right(final String right) {
        this.put(ATTR_RIGHT, right);
    }

    public String get__RightSd() {
        return this.getString(ATTR_RIGHT_SD);
    }

    public void set__RightSd(final String rightSd) {
        this.put(ATTR_RIGHT_SD, rightSd);
    }


    public Boolean is__Member() {
        return this.containsKey(ATTR_IS_MEMBER) && this.getBoolean(ATTR_IS_MEMBER);
    }

    public void set__Member(final Boolean isMember) {
        this.put(ATTR_IS_MEMBER, isMember);
    }


    /**
     * 获取实例展示内容。
     *
     * @return 实例展示内容
     */
    @Override
    public String getEntityDisplay() {
        SdEventDefinition sdEvent = UXDFLoader.getEvent(this.get__Sd(), this.get__LeftSd(), this.get__RightSd());
        return this.makeEntityDisplay(sdEvent);
    }

    @Override
    public EventEntity merge(SdEntity sdEntity) {
        sdEntity.forEach((propKey, propValue) -> {
            // 跳过不合并的属性
            if (UNSUPPORTED_MERGE_PROPS.contains(propKey)) {
                return;
            }
            this.put(propKey, propValue);
        });
        return this;
    }

    @Override
    public EventEntity id(final String id) {
        this.set__Id(id);
        return this;
    }

    @Override
    public EventEntity sd(final String sd) {
        this.set__Sd(sd);
        return this;
    }


    public EventEntity left(final String id) {
        this.set__Left(id);
        return this;
    }

    public EventEntity right(final String id) {
        this.set__Right(id);
        return this;
    }

    public EventEntity leftSd(final String sd) {
        this.set__LeftSd(sd);
        return this;
    }

    public EventEntity rightSd(final String sd) {
        this.set__RightSd(sd);
        return this;
    }

    public EventEntity leftNode(final NodeEntity nodeEntity) {
        this.set__Left(nodeEntity.get__Id());
        this.set__LeftSd(nodeEntity.get__Sd());
        return this;
    }

    public EventEntity rightNode(final NodeEntity nodeEntity) {
        this.set__Right(nodeEntity.get__Id());
        this.set__RightSd(nodeEntity.get__Sd());
        return this;
    }

    /**
     * 关联左Node的逻辑ID
     *
     * @return
     */
    public String leftLogicId() {
        return this.isEffective() ? this.get__LeftSd()
                + this.get__Left() : null;
    }

    /**
     * 关联右Node的逻辑ID
     *
     * @return
     */
    public String rightLogicId() {
        return this.isEffective() ? this.get__RightSd()
                + this.get__Right() : null;
    }

    @Override
    public StringBuilder makeBaseProperties() {
        StringBuilder builder = super.makeBaseProperties();
        if (builder == null) {
            return null;
        }

        if (this.get__Left() != null &&
                this.get__LeftSd() != null &&
                this.get__Right() != null &&
                this.get__RightSd() != null) {
            builder.append(this.get__Left())
                    .append(this.get__LeftSd())
                    .append(this.get__Right())
                    .append(this.get__RightSd());
            return builder;
        }

        return null;
    }

    @Override
    public boolean isEffective() {
        return this.get__Id() != null && this.makeBaseProperties() != null;
    }

    @Override
    public String generateUUID() {
        if (!this.isEffective()) {
            throw new UXDFException(
                    String.format(
                            "[%s]不是一个有效的Event。",
                            this.toJSONString()
                    )
            );
        }

        SdEventDefinition sdEvent = UXDFLoader.getEvent(this.get__Sd(), this.get__LeftSd(), this.get__RightSd());
        StringBuilder uuidBuilder = this.makeBaseProperties();
        if (sdEvent != null && sdEvent.getUniqueIndex() != null && sdEvent.getUniqueIndex().length > 0) {
            for (String index : sdEvent.getUniqueIndex()) {
                Object value = this.get(index);
                if (value == null) { // 唯一属性不完整，不能生成UUID
                    return null;
                }
                uuidBuilder.append(value);
            }
        }
        String uuid = UXDFHelper.generate(uuidBuilder.toString());
        this.set__Uuid(uuid);
        return uuid;
    }

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
        return obj != null && obj.getClass().equals(this.getClass()) && getLogicId().equals(((EventEntity) obj).getLogicId());
    }

    @Override
    public EventEntity clone() {
        EventEntity cloneEvent = new EventEntity(this);
        // 移除uuid，方式唯一属性变更后，uuid发生错误
        cloneEvent.remove(SdEntity.ATTR_UUID);
        return cloneEvent;
    }
}
