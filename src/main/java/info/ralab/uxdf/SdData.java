package info.ralab.uxdf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.ralab.uxdf.definition.SdEventDefinition;
import info.ralab.uxdf.definition.SdOperateType;
import info.ralab.uxdf.instance.EventEntity;
import info.ralab.uxdf.instance.NodeEntity;
import info.ralab.uxdf.instance.SdEntity;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;

/**
 * Sd 数据对象。包含了{@link NodeEntity}和{@link EventEntity}数据。<br />
 * <p>
 * <pre><code>
 * {
 *     "node": [NodeEntity],
 *     "event": {
 *         "eventName": [EventEntity]
 *     }
 * }
 * </code></pre>
 * </p>
 * 并提供数据唯一性校验能力。<br />
 * 进入{@link SdData}的数据都必须是有效数据{@link SdEntity#isEffective()}。
 */
@ToString
public class SdData {
    /**
     * Sd 数据对象中，Node部分的键。
     */
    public static final String KEY_NODE = "node";

    /**
     * Sd 数据对象中，Event部分的键。
     */
    public static final String KEY_EVENT = "event";

    /**
     * {@link NodeEntity}缓存，使用{@link NodeEntity}的hash值作为缓存的键。<br />
     * 每个hash值对应一个{@link NodeEntity}。
     */
    @JSONField(serialize = false, deserialize = false)
    private LinkedHashMap<String, NodeEntity> cacheNodeLogicId = Maps.newLinkedHashMap();

    /**
     * {@link EventEntity}缓存，使用{@link EventEntity}的hash值作为缓存的键。<br />
     * 每个hash值对应一个{@link EventEntity}。
     */
    @JSONField(serialize = false, deserialize = false)
    private LinkedHashMap<String, EventEntity> cacheEventLogicId = Maps.newLinkedHashMap();

    /**
     * {@link NodeEntity}缓存，使用{@link NodeEntity}的uuid值作为缓存的键。<br />
     * 每个uuid值对应一个{@link NodeEntity}。
     */
    @JSONField(serialize = false, deserialize = false)
    private LinkedHashMap<String, NodeEntity> cacheNodeUUID = Maps.newLinkedHashMap();

    /**
     * {@link EventEntity}缓存，使用{@link EventEntity}的uuid值作为缓存的键。<br />
     * 每个uuid值对应一个{@link EventEntity}。
     */
    @JSONField(serialize = false, deserialize = false)
    private LinkedHashMap<String, EventEntity> cacheEventUUID = Maps.newLinkedHashMap();

    /**
     * {@link EventEntity}缓存，使用{@link EventEntity}的name值作为缓存的键。<br />
     * 每个name值对应多个{@link EventEntity}
     */
    private Map<String, LinkedHashSet<EventEntity>> cacheEventName = Maps.newConcurrentMap();


    /**
     * {@link EventEntity}缓存，使用{@link NodeEntity}的logicId值作为缓存的键。<br />
     * 每个logicId值对应多个{@link EventEntity}
     */
    private Map<String, HashSet<String>> cacheNodeEvent = Maps.newConcurrentMap();

    /**
     * 获取所有{@link NodeEntity}，的一个深度copy集合。<br />
     * 对此集合的操作都不会反映在当前{@link SdData}中。<br />
     * 此方法内存开销和时间开销都较大。
     *
     * @return 深度copy后的NodeEntity集合。
     */
    @JSONField(serialize = false, deserialize = false)
    public List<NodeEntity> getDetachedNode() {
        List<NodeEntity> nodeEntities = Lists.newArrayListWithCapacity(cacheNodeLogicId.size());
        cacheNodeLogicId.forEach((nodeHash, nodeEntity) -> nodeEntities.add(nodeEntity.clone()));
        return nodeEntities;
    }

    /**
     * 获取和Event相关的Node的深度copy集合。<br />
     * 对此集合的操作都不会反映在当前{@link SdData}中。<br />
     * <p>
     * 当Event为{@link null}，或者Event无效{@link EventEntity#isEffective()}。将返回一个空的Node集合。
     * </p>
     *
     * @param eventEntity Event
     * @return Node集合
     */
    @JSONField(serialize = false, deserialize = false)
    public List<NodeEntity> getDetachedNode(final EventEntity eventEntity) {
        List<NodeEntity> results = Lists.newArrayList();
        // event为空或event无效，返回空的node集合
        if (eventEntity == null || !eventEntity.isEffective()) {
            return results;
        }

        String leftLogicId = eventEntity.leftLogicId();
        if (this.cacheNodeLogicId.containsKey(leftLogicId)) {
            // 添加相关Node的clone副本。
            results.add(this.cacheNodeLogicId.get(leftLogicId).clone());
        }
        String rightLogicId = eventEntity.rightLogicId();
        if (this.cacheNodeLogicId.containsKey(rightLogicId)) {
            // 添加相关Node的clone副本。
            results.add(this.cacheNodeLogicId.get(rightLogicId).clone());
        }

        return results;
    }

    /**
     * 获取一个不可修改{@link NodeEntity}集合。该集合不能添加、移除集合中的成员。<br />
     * <p>
     * 此方法获取效率较高。<br />
     * </p>
     * <p>
     * <b>集合虽然不可修改，但集合中的{@link NodeEntity}可以需改。
     * 这种修改有可能引起{@link SdData}中索引失效，所以不建议通过此集合直接需改其中的{@link NodeEntity}。</b>
     * </p>
     *
     * @return {@link SdData}中所有Node的集合。
     */
    @JSONField(name = "node", deserialize = false)
    public List<NodeEntity> getUnmodifiableNode() {
        return Collections.unmodifiableList(Lists.newArrayList(cacheNodeLogicId.values()));
    }

    /**
     * 重新设置{@link SdData}的所有Node。<br />
     * <p>
     * 会清空原有所有Node，并使用新的Node重建索引。<br />
     * 新集合中的Node如果是无效{@link NodeEntity#isEffective()}的Node。不会被添加到{@link SdData}中。
     * </p>
     *
     * @param node Node集合
     */
    @JSONField(name = "node")
    public void setNode(final List<NodeEntity> node) {
        // 清空已有集合
        this.clearNode();
        if (node == null || node.isEmpty()) {
            return;
        }
        // 将新Node集合中Node加入
        node.forEach(this::addNodeIfAbsent);
    }

    /**
     * 获取所有{@link EventEntity}的一个深度copy集合。<br />
     * 对此集合的操作都不会反映在当前{@link SdData}中。<br />
     * 此方法内存开销和时间开销都较大。
     *
     * @return EventEntity集合
     */
    @JSONField(serialize = false, deserialize = false)
    public Map<String, List<EventEntity>> getDetachedEvent() {
        Map<String, List<EventEntity>> event = Maps.newHashMap();
        this.cacheEventName.forEach((eventName, events) -> {
            List<EventEntity> eventEntities = Lists.newArrayListWithCapacity(events.size());
            events.forEach(eventEntity -> eventEntities.add(eventEntity.clone()));
            event.put(eventName, eventEntities);
        });
        return event;
    }

    /**
     * 通过Event名称，获取一个{@link EventEntity}的深度copy集合。<br />
     * 对此集合的操作都不会反映在当前{@link SdData}中。<br />
     * 此方法内存开销和时间开销都较大。
     *
     * @param eventSd Event名称
     * @return {@link SdData}中对应Event名称的所有{@link EventEntity}的集合。
     */
    @JSONField(serialize = false, deserialize = false)
    public List<EventEntity> getDetachedEvent(final String eventSd) {
        if (this.cacheEventName.containsKey(eventSd)) {
            LinkedHashSet<EventEntity> eventSource = cacheEventName.get(eventSd);
            List<EventEntity> eventTarget = Lists.newArrayList(new EventEntity[eventSource.size()]);
            Collections.copy(eventTarget, Lists.newArrayList(eventSource));
            return eventTarget;
        } else {
            return Lists.newArrayList();
        }
    }

    /**
     * 获取和{@link NodeEntity}相关的{@link EventEntity}。
     * <p>
     * 如果{@link NodeEntity}为NULL或者无效{@link NodeEntity#isEffective()}，将返回空集合。
     * </p>
     *
     * @param nodeEntity Node
     * @return EventEntity集合
     */
    @JSONField(serialize = false, deserialize = false)
    public List<EventEntity> getDetachedEvent(final NodeEntity nodeEntity) {
        List<EventEntity> results = Lists.newArrayList();
        if (nodeEntity == null || !nodeEntity.isEffective() || !this.cacheNodeEvent.containsKey(nodeEntity.getLogicId())) {
            return results;
        }

        this.cacheNodeEvent.get(nodeEntity.getLogicId()).forEach(eventHashCode -> {
            if (this.cacheEventLogicId.containsKey(eventHashCode)) {
                EventEntity eventEntity = this.cacheEventLogicId.get(eventHashCode);
                results.add(eventEntity.clone());
            }
        });

        return results;
    }

    /**
     * 获取一个不可修改{@link EventEntity}集合。该集合不能添加、移除集合中的成员。<br />
     * <p>
     * 此方法获取效率较高。<br />
     * </p>
     * <p>
     * <b>集合虽然不可修改，但集合中的{@link EventEntity}可以需改。
     * 这种修改有可能引起{@link SdData}中索引失效，所以不建议通过此集合直接需改其中的{@link EventEntity}。</b>
     * </p>
     *
     * @return {@link SdData}中所有{@link EventEntity}的集合。
     */
    @JSONField(name = "event", deserialize = false)
    public Map<String, List<EventEntity>> getUnmodifiableEvent() {
        Map<String, List<EventEntity>> event = Maps.newHashMap();
        this.cacheEventName.forEach((eventName, events) -> event.put(eventName, Collections.unmodifiableList(Lists.newArrayList(events))));
        return Collections.unmodifiableMap(event);
    }

    /**
     * 根据Event定义名称，获取一个不可修改的{@link EventEntity}集合。该集合不能添加、移除集合中的成员。<br />
     * <p>
     * 此方法获取效率较高。<br />
     * </p>
     * <p>
     * <b>集合虽然不可修改，但集合中的{@link EventEntity}可以需改。
     * 这种修改有可能引起{@link SdData}中索引失效，所以不建议通过此集合直接需改其中的{@link EventEntity}。</b>
     * </p>
     *
     * @return {@link SdData}中对应Event定义名称{@link EventEntity}的集合。
     */
    @JSONField(serialize = false, deserialize = false)
    public List<EventEntity> getUnmodifiableEvent(final String eventSd) {
        return Collections.unmodifiableList(Lists.newArrayList(cacheEventName.getOrDefault(eventSd, Sets.newLinkedHashSet())));
    }

    /**
     * 重新设置{@link SdData}的所有{@link EventEntity}。<br />
     * <p>
     * 会清空原有所有{@link EventEntity}，并使用新的{@link EventEntity}重建索引。<br />
     * 新集合中的{@link EventEntity}如果是无效{@link EventEntity#isEffective()}的。则不会被添加到{@link SdData}中。
     * </p>
     *
     * @param event Event集合
     */
    @JSONField(name = "event")
    public void setEvent(final Map<String, List<EventEntity>> event) {
        this.clearEvent();
        if (event == null || event.isEmpty()) {
            return;
        }
        event.forEach((eventName, eventList) -> eventList.forEach(this::addEventIfAbsent));
    }

    /**
     * 当前{@link SdData}是否没有数据。
     *
     * @return 是否没有数据。
     */
    public boolean isEmpty() {
        return isNodeEmpty() && isEventEmpty();
    }

    /**
     * 当前{@link SdData}是否没有{@link NodeEntity}数据。
     *
     * @return 是否没有Node数据
     */
    public boolean isNodeEmpty() {
        return cacheNodeLogicId.isEmpty();
    }

    /**
     * 当前{@link SdData}是否没有{@link EventEntity}数据。
     *
     * @return 是否没有Event数据
     */
    public boolean isEventEmpty() {
        return cacheEventLogicId.isEmpty();
    }

    /**
     * 从另一个{@link SdData}实例中，合并数据到当前{@link SdData}实例。
     *
     * @param sdData 被合并的目标SdData。
     */
    public synchronized void merge(final SdData sdData) {
        if (sdData == null || sdData.isEmpty()) {
            return;
        }

        // 合并Node
        sdData.cacheNodeLogicId.values().forEach(this::addNodeIfAbsent);
        // 合并Event
        sdData.cacheEventLogicId.values().forEach(this::addEventIfAbsent);
    }

    /**
     * 设置{@link SdData}中所有数据的操作类型{@link SdOperateType}
     *
     * @param operate 操作类型
     */
    public void setOperate(final SdOperateType operate) {
        this.cacheNodeLogicId.values().forEach(nodeEntity -> nodeEntity.setOperate(operate));
        this.cacheEventLogicId.values().forEach(eventEntity -> eventEntity.setOperate(operate));
    }

    /**
     * 添加{@link NodeEntity}到{@link SdData}，如果{@link NodeEntity}已经存在则不添加。<br />
     * 添加的数据必须是一个有效的{@link NodeEntity}。<br />
     * 当前方法是一个同步方法。
     *
     * @param nodeEntity 要添加的Node
     * @return 返回是否添加成功
     */
    public boolean addNodeIfAbsent(final NodeEntity nodeEntity) {
        return this.addNode(nodeEntity, false);
    }

    /**
     * 添加{@link NodeEntity}到{@link SdData}，如果{@link NodeEntity}已经存在，则覆盖已存在。<br />
     * 添加的数据必须是一个有效的{@link NodeEntity}。<br />
     * 当前方法是一个同步方法。
     *
     * @param nodeEntity 要添加的Node
     * @return 返回是否添加成功
     */
    public boolean overwriteNode(final NodeEntity nodeEntity) {
        return this.addNode(nodeEntity, true);
    }

    /**
     * 添加{@link NodeEntity}到{@link SdData}，可以指定是否覆盖已存在数据。<br />
     * 添加的数据必须是一个有效的{@link NodeEntity}。<br />
     * 当前方法是一个同步方法。
     *
     * @param nodeEntity 要添加的Node
     * @param overwrite  是否覆盖已存在数据
     * @return 返回是否添加成功
     */
    private synchronized boolean addNode(final NodeEntity nodeEntity, final boolean overwrite) {
        if (nodeEntity == null ||
                !nodeEntity.isEffective() ||
                !overwrite && this.cacheNodeLogicId.containsKey(nodeEntity.getLogicId())) { // 不允许覆盖 并且 存在
            return false;
        }
        // 允许覆盖，先移除已有数据
        if (overwrite) {
            this.removeNode(nodeEntity);
        }
        // UUID缓存
        String uuid = nodeEntity.generateUUID();
        // 可以生成有效的UUID
        if (uuid != null) {
            // 检查是否存在逻辑主键不同，但是业务主键相同的数据
            if (this.cacheNodeUUID.containsKey(uuid) && !this.cacheNodeUUID.get(uuid).equals(nodeEntity)) {
                throw new UXDFException(
                        String.format(
                                "当前数据集中已经存在uuid和要添加Node[%s]相同的数据[%s]。",
                                nodeEntity,
                                this.cacheNodeUUID.get(uuid)
                        )
                );
            }
            this.cacheNodeUUID.put(uuid, nodeEntity);
        }
        // hash缓存
        this.cacheNodeLogicId.put(nodeEntity.getLogicId(), nodeEntity);
        return true;
    }

    /**
     * 更新{@link NodeEntity}。
     * <p>
     * 替换sourceLogicId和sourceId对应的{@link NodeEntity}。<br />
     * 仅当sourceLogicId和sourceId对应的{@link NodeEntity}存在，
     * 并且其__sd、__repository、__branch、__version属性和更新后的{@link NodeEntity}一致时，认为可以更新。
     * </p>
     * <p>
     * 同时会更新Node和Event对应的映射关系。
     * </p>
     *
     * @param sourceLogicId 要被更新的{@link NodeEntity}的源logicId
     * @param sourceId      要被更新的{@link NodeEntity}的源id
     * @param targetNode    要被更新的目标{@link NodeEntity}
     * @return 更新成功后的NodeEntity
     */
    public synchronized NodeEntity updateNode(final String sourceLogicId, final String sourceId, final NodeEntity targetNode) {
        if (targetNode == null || !targetNode.isEffective()) {
            throw new UXDFException("更新的目标NodeEntity无效。");
        }

        // 源不存在
        if (!this.cacheNodeLogicId.containsKey(sourceLogicId)) {
            throw new UXDFException(String.format("更新Node的源[%s]不存在。", sourceLogicId));
        }

        // 获取源Node
        NodeEntity sourceNode = this.cacheNodeLogicId.get(sourceLogicId);

        // 源Node的id和源id不匹配
        if (!sourceId.equals(sourceNode.get__Id())) {
            throw new UXDFException(String.format("更新Node的源id[%s]不匹配。", sourceId));
        }

        // 检查要更新的目标是否已经存在
        final String targetLogicId = targetNode.getLogicId();
        if (!sourceLogicId.equals(targetLogicId) && this.cacheNodeLogicId.containsKey(targetLogicId)) {
            throw new UXDFException(String.format("更新Node的目标[%s]已经存在。", targetLogicId));
        }

        // 判断源和目标的类型以及版本信息一致
        if (!sourceNode.get__Sd().equals(targetNode.get__Sd())) {
            throw new UXDFException(String.format("源Node[%s]和目标Node[%s]的类型或版本信息不一致。", sourceNode, targetNode));
        }

        // 从logicId缓存中移除
        this.cacheNodeLogicId.remove(sourceLogicId);
        // 加入logicId缓存
        this.cacheNodeLogicId.put(targetLogicId, targetNode);

        // 获取源uuid
        String sourceUUID = sourceNode.getUUID();
        // 从UUID缓存中移除
        if (sourceUUID != null) {
            this.cacheNodeUUID.remove(sourceUUID);
        }

        // 获取目标uui
        String targetUUID = targetNode.generateUUID();
        // 加入UUID缓存
        if (targetUUID != null) {
            this.cacheNodeUUID.put(targetUUID, targetNode);
        }

        // 获取和Event的映射缓存
        Set<String> eventLogicIds = this.cacheNodeEvent.remove(sourceLogicId);
        // 更新和Event的映射缓存
        if (eventLogicIds != null) {
            HashSet<String> targetEventLogicIds = Sets.newHashSet();
            eventLogicIds.forEach((eventLogicId) -> {
                EventEntity eventEntity = this.cacheEventLogicId.get(eventLogicId);
                // 更新Event中的关联Node的id
                if (eventEntity.leftLogicId().equals(sourceLogicId)) {
                    // 源Node是左节点
                    String sourceEventLogicId = eventEntity.getLogicId();
                    String sourceEventUUID = eventEntity.getUUID();

                    // 更新缓存
                    this.cacheEventLogicId.remove(sourceEventLogicId);
                    this.cacheEventUUID.remove(sourceEventUUID);
                    this.cacheEventName.get(eventEntity.get__Sd()).remove(eventEntity);
                    this.cacheNodeEvent.get(eventEntity.rightLogicId()).remove(eventLogicId);

                    // 更新左节点
                    eventEntity.leftNode(targetNode);

                    // 加入映射缓存
                    targetEventLogicIds.add(eventEntity.getLogicId());
                    this.cacheEventLogicId.put(eventEntity.getLogicId(), eventEntity);
                    this.cacheEventUUID.put(eventEntity.generateUUID(), eventEntity);
                    this.cacheEventName.get(eventEntity.get__Sd()).add(eventEntity);
                    this.cacheNodeEvent.get(eventEntity.rightLogicId()).add(eventEntity.getLogicId());
                } else if (eventEntity.rightLogicId().equals(sourceLogicId)) {
                    // 源Node是右节点
                    String sourceEventLogicId = eventEntity.getLogicId();
                    String sourceEventUUID = eventEntity.getUUID();

                    // 更新缓存
                    this.cacheEventLogicId.remove(sourceEventLogicId);
                    this.cacheEventUUID.remove(sourceEventUUID);
                    this.cacheEventName.get(eventEntity.get__Sd()).remove(eventEntity);
                    this.cacheNodeEvent.get(eventEntity.leftLogicId()).remove(eventLogicId);

                    // 更新右节点
                    eventEntity.rightNode(targetNode);

                    // 加入映射缓存
                    targetEventLogicIds.add(eventEntity.getLogicId());
                    this.cacheEventLogicId.put(eventEntity.getLogicId(), eventEntity);
                    this.cacheEventUUID.put(eventEntity.generateUUID(), eventEntity);
                    this.cacheEventName.get(eventEntity.get__Sd()).add(eventEntity);
                    this.cacheNodeEvent.get(eventEntity.leftLogicId()).add(eventEntity.getLogicId());
                }
            });
            // 替换映射缓存
            this.cacheNodeEvent.put(targetLogicId, targetEventLogicIds);
        }

        // 用目标覆盖源的内容
        sourceNode
                .fluentClear()
                .fluentPutAll(targetNode);

        return targetNode;
    }

    /**
     * 基于{@link NodeEntity#getLogicId()}从{@link SdData}中移除当前{@link NodeEntity}。<br />
     * 当前方法是一个同步方法。
     *
     * @param nodeEntity 需要被移除的Node
     * @return 返回成功被移除的Node
     */
    public synchronized NodeEntity removeNode(final NodeEntity nodeEntity) {
        if (nodeEntity == null || !nodeEntity.isEffective()) {
            return null;
        }
        // 获取逻辑ID
        final String logicId = nodeEntity.getLogicId();
        // 逻辑ID没有对应的Node
        if (!this.cacheNodeLogicId.containsKey(logicId)) {
            return null;
        }

        // 从逻辑ID缓存中移除
        NodeEntity removedNode = this.cacheNodeLogicId.remove(logicId);

        // 从UUID缓存中移除
        final String uuid = removedNode.getUUID();
        this.cacheNodeUUID.remove(uuid);

        return removedNode;
    }


    /**
     * 基于{@link NodeEntity#getUUID()}从{@link SdData}中移除当前{@link NodeEntity}。<br />
     * 当前方法是一个同步方法。
     *
     * @param uuid 需要被移除的Node的UUID
     * @return 返回成功被移除的Node
     */
    public synchronized NodeEntity removeNode(final String uuid) {
        // 如果uuid在缓存中不存在，返回null
        if (!this.cacheNodeUUID.containsKey(uuid)) {
            return null;
        }

        // 从UUID缓存中移除
        NodeEntity removedNode = this.cacheNodeUUID.remove(uuid);

        // 从Logic缓存中移除
        final String logicId = removedNode.getLogicId();
        this.cacheNodeLogicId.remove(logicId);

        return removedNode;
    }

    /**
     * 基于{@link NodeEntity#getLogicId()}获取{@link SdData}中对应的数据。<br />
     * 如果没有对应logicId的Node，则返回NULL。
     *
     * @param nodeEntity 用于提供logicId的Node
     * @return 对应logicId的Node
     */
    public NodeEntity getNode(final NodeEntity nodeEntity) {
        return nodeEntity == null || !nodeEntity.isEffective() ? null : this.cacheNodeLogicId.get(nodeEntity.getLogicId());
    }

    /**
     * 基于{@link NodeEntity#getLogicId()}获取{@link SdData}中对应的数据。<br />
     * 如果没有对应logicId的Node，则返回NULL。
     *
     * @param logicId 用于提供logicId的Node
     * @return 对应logicId的Node
     */
    public NodeEntity getNodeByLogicId(final String logicId) {
        return StringUtils.isBlank(logicId) ? null : this.cacheNodeLogicId.get(logicId);
    }

    /**
     * 基于{@link NodeEntity#getUUID()}从{@link SdData}中获取对应的{@link NodeEntity}。<br />
     * 如果没有对应的{@link NodeEntity}，则返回NULL。
     *
     * @param uuid Node的UUID
     * @return 对应UUID的Node
     */
    public NodeEntity getNode(final String uuid) {
        return this.cacheNodeUUID.get(uuid);
    }

    /**
     * 清空{@link NodeEntity}所有相关缓存。<br />
     * 这是一个同步方法。
     */
    public synchronized void clearNode() {
        this.cacheNodeLogicId.clear();
        this.cacheNodeUUID.clear();
    }

    /**
     * 添加{@link EventEntity}到{@link SdData}，如果{@link EventEntity}已经存在则不添加。<br />
     * 要添加的{@link EventEntity}必须是有效的{@link EventEntity#isEffective()}。
     * 这是一个同步方法。
     *
     * @param eventEntity 要添加到SdData的Event。
     */
    public boolean addEventIfAbsent(final EventEntity eventEntity) {
        return this.addEvent(eventEntity, false);
    }

    /**
     * 添加Event到数据集合，如果Event已经存在，则覆盖已存在
     *
     * @param eventEntity
     */
    public boolean overwriteEvent(final EventEntity eventEntity) {
        return this.addEvent(eventEntity, true);
    }

    /**
     * 添加{@link EventEntity}到{@link SdData}，可以指定是否覆盖已存在数据。<br />
     * 添加的数据必须是一个有效的{@link EventEntity}。<br />
     * 当前方法是一个同步方法。
     *
     * @param eventEntity 要添加的Event
     * @param overwrite   是否覆盖已存在数据
     * @return 返回是否添加成功
     */
    private synchronized boolean addEvent(final EventEntity eventEntity, final boolean overwrite) {
        if (eventEntity == null ||
                !eventEntity.isEffective() ||
                !overwrite && this.cacheEventLogicId.containsKey(eventEntity.getLogicId())) { // 不允许覆盖 并且 存在
            return false;
        }
        // 如果覆盖，先删除已有数据
        if (overwrite) {
            this.removeEvent(eventEntity);
        }

        // UUID缓存
        final String uuid = eventEntity.generateUUID();

        //isMember属性
        SdEventDefinition sdEvent = UXDFLoader.getEvent(eventEntity.get__Sd(), eventEntity.get__LeftSd(), eventEntity.get__RightSd());
        eventEntity.set__Member(sdEvent != null && sdEvent.getIsMember());

        // 可以生成业务主键
        if (uuid != null) {
            // 检查是否存在逻辑主键不同，但业务主键一致的数据
            if (this.cacheEventUUID.containsKey(uuid) && !this.cacheEventUUID.get(uuid).equals(eventEntity)) {
                throw new UXDFException(
                        String.format(
                                "当前数据集中已经存在uuid和要添加Event[%s]相同的数据[%s]。",
                                eventEntity,
                                this.cacheEventUUID.get(uuid)
                        )
                );

            }
            this.cacheEventUUID.put(uuid, eventEntity);
        }

        // EventSd缓存
        final String eventSd = eventEntity.get__Sd();
        LinkedHashSet<EventEntity> eventEntities = this.cacheEventName.computeIfAbsent(
                eventSd,
                (key) -> Sets.newLinkedHashSet()
        );
        eventEntities.add(eventEntity);

        // Hash缓存
        this.cacheEventLogicId.put(eventEntity.getLogicId(), eventEntity);

        // 建立和Node之间的缓存关系
        final String leftNodeHash = eventEntity.leftLogicId();
        this.cacheNodeEvent.computeIfAbsent(leftNodeHash, key -> Sets.newHashSet()).add(eventEntity.getLogicId());
        final String rightNodeHash = eventEntity.rightLogicId();
        this.cacheNodeEvent.computeIfAbsent(rightNodeHash, key -> Sets.newHashSet()).add(eventEntity.getLogicId());

        return true;
    }

    /**
     * 更新{@link EventEntity}。
     * <p>
     * 替换sourceLogicId和sourceId对应的{@link EventEntity}。<br />
     * 仅当sourceLogicId和sourceId对应的{@link EventEntity}存在，
     * 并且其__sd、__repository、__branch、__version属性和更新目标的{@link EventEntity}一致时，认为可以更新。
     * </p>
     * <p>
     * 同时会更新Node和Event对应的映射关系。
     * </p>
     *
     * @param sourceLogicId
     * @param sourceId
     * @param targetEvent
     * @return
     */
    public EventEntity updateEvent(final String sourceLogicId, final String sourceId, final EventEntity targetEvent) {

        if (targetEvent == null || !targetEvent.isEffective()) {
            throw new UXDFException("更新的目标EventEntity无效。");
        }

        // 源不存在
        if (!this.cacheEventLogicId.containsKey(sourceLogicId)) {
            throw new UXDFException(String.format("更新Event的源[%s]不存在。", sourceLogicId));
        }

        // 获取源Event
        EventEntity sourceEvent = this.cacheEventLogicId.get(sourceLogicId);

        // 源Event的id和源id不匹配
        if (!sourceId.equals(sourceEvent.get__Id())) {
            throw new UXDFException(String.format("更新Event的源id[%s]不匹配。", sourceId));
        }

        // 检查要更新的目标是否已经存在
        final String targetLogicId = targetEvent.getLogicId();
        if (!sourceLogicId.equals(targetLogicId) && this.cacheEventLogicId.containsKey(targetLogicId)) {
            throw new UXDFException(String.format("更新Event的目标[%s]已经存在。", targetLogicId));
        }

        // 判断源和目标的类型以及版本信息一致
        if (!sourceEvent.get__Sd().equals(targetEvent.get__Sd())) {
            throw new UXDFException(String.format("源Event[%s]和目标Event[%s]的类型或版本信息不一致。", sourceEvent, targetEvent));
        }

        // 从LogicId缓存中移除
        this.cacheEventLogicId.remove(sourceLogicId);
        // 加入LogicId缓存
        this.cacheEventLogicId.put(targetLogicId, targetEvent);

        // 获取源uuid
        String sourceUUID = sourceEvent.getUUID();
        // 从UUID缓存中移除
        if (sourceUUID != null) {
            this.cacheEventUUID.remove(sourceUUID);
        }
        // 获取目标uuid
        String targetUUID = targetEvent.generateUUID();
        // 加入UUID缓存
        if (targetUUID != null) {
            this.cacheEventUUID.put(targetUUID, targetEvent);
        }


        // 获取和Event的映射缓存
        Set<String> leftEventLogicIds = this.cacheNodeEvent.remove(sourceEvent.leftLogicId());
        // 移除左Node中Event的映射缓存
        if (leftEventLogicIds != null) {
            leftEventLogicIds.remove(sourceLogicId);
        }
        Set<String> rightEventLogicIds = this.cacheNodeEvent.remove(sourceEvent.rightLogicId());
        // 移除右Node中Event的映射缓存
        if (rightEventLogicIds != null) {
            rightEventLogicIds.remove(sourceLogicId);
        }

        // 加入左Node的Event映射缓存
        this.cacheNodeEvent.compute(targetEvent.leftLogicId(), (key, logicIds) -> {
            if (logicIds == null) {
                logicIds = Sets.newHashSet();
            }

            logicIds.add(targetLogicId);

            return logicIds;
        });

        // 加入右Node的Event映射缓存
        this.cacheNodeEvent.compute(targetEvent.rightLogicId(), (key, logicIds) -> {
            if (logicIds == null) {
                logicIds = Sets.newHashSet();
            }

            logicIds.add(targetLogicId);

            return logicIds;
        });


        // 移除Event名称缓存
        this.cacheEventName.get(sourceEvent.get__Sd()).remove(sourceEvent);
        // 加入Event名称缓存
        this.cacheEventName.get(targetEvent.get__Sd()).add(targetEvent);

        // 用目标内容覆盖源内容
        sourceEvent.fluentClear().fluentPutAll(targetEvent);

        return targetEvent;
    }

    /**
     * 基于{@link EventEntity#getLogicId()}从{@link SdData}中移除当前{@link EventEntity}。<br />
     * 当前方法是一个同步方法。
     *
     * @param eventEntity 需要被移除的Event
     * @return 返回成功被移除的Node
     */
    public synchronized EventEntity removeEvent(final EventEntity eventEntity) {
        if (eventEntity == null || !eventEntity.isEffective()) {
            return null;
        }

        final String logicId = eventEntity.getLogicId();
        final String uuid = eventEntity.getUUID();

        // 根据logic id移除
        EventEntity removedEvent = this.cacheEventLogicId.remove(logicId);

        // 根据uuid移除
        EventEntity uuidRemovedEvent = this.cacheEventUUID.remove(uuid);

        if (removedEvent == null && uuidRemovedEvent != null) {
            removedEvent = uuidRemovedEvent;
        }

        if (removedEvent == null) {
            return null;
        }

        // 从event类型缓存中移除
        this.cacheEventName.get(eventEntity.get__Sd()).remove(removedEvent);

        // 从node和event映射中移除
        String leftHashCode = removedEvent.leftLogicId();
        if (this.cacheNodeEvent.containsKey(leftHashCode)) {
            this.cacheNodeEvent.get(leftHashCode).remove(removedEvent.getLogicId());
        }
        String rightHashCode = removedEvent.rightLogicId();
        if (this.cacheNodeEvent.containsKey(rightHashCode)) {
            this.cacheNodeEvent.get(rightHashCode).remove(removedEvent.getLogicId());
        }
        return removedEvent;
    }

    /**
     * 基于{@link EventEntity#getUUID()}从{@link SdData}中移除当前{@link EventEntity}。<br />
     * 当前方法是一个同步方法。
     *
     * @param uuid 需要被移除的Event的UUID
     * @return 返回成功被移除的Event
     */
    public synchronized EventEntity removeEvent(final String uuid) {
        if (!this.cacheEventUUID.containsKey(uuid)) {
            return null;
        }

        // 根据uuid移除
        EventEntity removedEvent = this.cacheEventUUID.remove(uuid);
        this.cacheEventLogicId.remove(removedEvent.getLogicId());

        // 从node名称映射中移除
        this.cacheEventName.get(removedEvent.get__Sd()).remove(removedEvent);

        // 从node和event映射中移除
        String leftHashCode = removedEvent.leftLogicId();
        if (this.cacheNodeEvent.containsKey(leftHashCode)) {
            this.cacheNodeEvent.get(leftHashCode).remove(removedEvent.getLogicId());
        }
        String rightHashCode = removedEvent.rightLogicId();
        if (this.cacheNodeEvent.containsKey(rightHashCode)) {
            this.cacheNodeEvent.get(rightHashCode).remove(removedEvent.getLogicId());
        }
        return removedEvent;
    }

    /**
     * 基于{@link EventEntity#getLogicId()}获取{@link SdData}中对应的数据。<br />
     * 如果没有对应logicId的Node，则返回NULL。
     *
     * @param eventEntity 用于提供logicId的Event
     * @return 对应logicId的Event
     */
    public EventEntity getEvent(final EventEntity eventEntity) {
        return eventEntity == null || !eventEntity.isEffective() ? null : this.cacheEventLogicId.get(eventEntity.getLogicId());
    }

    /**
     * 基于{@link EventEntity#getUUID()}从{@link SdData}中获取对应的{@link EventEntity}。<br />
     * 如果没有对应的{@link EventEntity}，则返回NULL。
     *
     * @param uuid Event的UUID
     * @return 对应UUID的Event
     */
    public EventEntity getEvent(final String uuid) {
        return this.cacheEventUUID.get(uuid);
    }

    /**
     * 清空{@link EventEntity}所有相关缓存。<br />
     * 这是一个同步方法。
     */
    public synchronized void clearEvent() {
        this.cacheEventName.clear();
        this.cacheEventLogicId.clear();
        this.cacheEventUUID.clear();
        this.cacheNodeEvent.clear();
    }

    /**
     * 根据要添加的{@link NodeEntity}的logicId，获取当前数据集合中对应的已有{@link NodeEntity}。<br />
     * 保存经过计算后的{@link NodeEntity}。<br />
     * 如果计算后的{@link NodeEntity}为NULL，则从当前集合中移除对应的{@link NodeEntity}。
     *
     * @param newNodeEntity 用于提供logicId的Node
     * @param nodeFunction  用于计算使用的function
     */
    public void computeNode(
            final NodeEntity newNodeEntity,
            final Function<NodeEntity, NodeEntity> nodeFunction
    ) {
        String newLogicId = newNodeEntity.getLogicId();
        NodeEntity existNode = null;
        if (this.cacheNodeLogicId.containsKey(newLogicId)) {
            existNode = this.cacheNodeLogicId.get(newLogicId);
        }
        NodeEntity nodeEntity = nodeFunction.apply(existNode);
        if (nodeEntity == null) { // 计算返回结果为空，则删除对应UUID和数据
            this.removeNode(newNodeEntity);
        } else { // 覆盖已有
            this.overwriteNode(nodeEntity);
        }
    }

    /**
     * 根据uuid，获取当前数据集合中对应的已有{@link NodeEntity}。<br />
     * 保存经过计算后的{@link NodeEntity}。<br />
     * 如果计算后的{@link NodeEntity}为NULL，则从当前集合中移除对应的{@link NodeEntity}。
     *
     * @param uuid         Node的UUID
     * @param nodeFunction 用于计算使用的function
     */
    public void computeNode(
            final String uuid,
            final Function<NodeEntity, NodeEntity> nodeFunction
    ) {
        NodeEntity existNode = null;
        if (this.cacheNodeUUID.containsKey(uuid)) {
            existNode = this.cacheNodeUUID.get(uuid);
        }
        NodeEntity nodeEntity = nodeFunction.apply(existNode);
        if (nodeEntity == null) { // 计算返回结果为空，则删除对应UUID和数据
            this.removeNode(uuid);
        } else { // 覆盖已有
            this.overwriteNode(nodeEntity);
        }
    }

    /**
     * 根据要添加的{@link EventEntity}的logicId，获取当前数据集合中对应的已有{@link EventEntity}。<br />
     * 保存经过计算后的{@link EventEntity}。<br />
     * 如果计算后的{@link EventEntity}为NULL，则从当前集合中移除对应的{@link EventEntity}。
     *
     * @param newEventEntity 用于提供logicId的Event
     * @param eventFunction  用于计算使用的function
     */
    public void computeEvent(
            final EventEntity newEventEntity,
            final Function<EventEntity, EventEntity> eventFunction
    ) {
        String newLogicId = newEventEntity.getLogicId();
        EventEntity existEvent = null;
        if (this.cacheEventLogicId.containsKey(newLogicId)) {
            existEvent = this.cacheEventLogicId.get(newLogicId);
        }
        EventEntity eventEntity = eventFunction.apply(existEvent);
        if (eventEntity == null) { // 计算返回结果为空，则删除对应UUID和数据
            this.removeEvent(newEventEntity);
        } else { // 覆盖已有
            this.overwriteEvent(eventEntity);
        }
    }

    /**
     * 根据uuid，获取当前数据集合中对应的已有{@link EventEntity}。<br />
     * 保存经过计算后的{@link EventEntity}。<br />
     * 如果计算后的{@link EventEntity}为NULL，则从当前集合中移除对应的{@link EventEntity}。
     *
     * @param uuid          Event的UUID
     * @param eventFunction 用于计算使用的function
     */
    public void computeEvent(
            final String uuid,
            final Function<EventEntity, EventEntity> eventFunction
    ) {
        EventEntity existEvent = null;
        if (this.cacheEventUUID.containsKey(uuid)) {
            existEvent = this.cacheEventUUID.get(uuid);
        }
        EventEntity eventEntity = eventFunction.apply(existEvent);
        if (eventEntity == null) { // 计算返回结果为空，则删除对应UUID和数据
            this.removeEvent(uuid);
        } else { // 覆盖已有
            this.overwriteEvent(eventEntity);
        }
    }

    /**
     * 深度复制当前{@link SdData}的副本。
     *
     * @return
     */
    @Override
    public SdData clone() {
        // TODO 此处需要处理超大数据集情况
        return JSON.parseObject(JSON.toJSONString(this), this.getClass());
    }

    /**
     * 判断当前{@link SdData}是否包含{@link NodeEntity}，基于logicId
     *
     * @param nodeEntity Node
     * @return 返回是否包含
     */
    public boolean containsNode(final NodeEntity nodeEntity) {
        if (nodeEntity == null || !nodeEntity.isEffective()) {
            return false;
        }
        return this.cacheNodeLogicId.containsKey(nodeEntity.getLogicId());
    }

    /**
     * 判断当前{@link SdData}是否包含{@link NodeEntity}，基于uuid
     *
     * @param uuid UUID
     * @return 返回是否包含
     */
    public boolean containsNode(final String uuid) {
        return this.cacheNodeUUID.containsKey(uuid);
    }

    /**
     * 判断当前{@link SdData}是否包含{@link EventEntity}，基于logicId
     *
     * @param eventEntity Event
     * @return 返回是否包含
     */
    public boolean containsEvent(final EventEntity eventEntity) {
        if (eventEntity == null || !eventEntity.isEffective()) {
            return false;
        }
        return this.cacheEventLogicId.containsKey(eventEntity.getLogicId());
    }

    /**
     * 判断当前{@link SdData}是否包含{@link NodeEntity}，基于uuid
     *
     * @param uuid UUID
     * @return 返回是否包含
     */
    public boolean containsEvent(final String uuid) {
        return this.cacheEventUUID.containsKey(uuid);
    }

    /**
     * 判断当前{@link SdData}是否包含Event的名称
     *
     * @param eventSd Event的名称
     * @return 返回是否包含
     */
    public boolean containsEventSd(final String eventSd) {
        return cacheEventName.containsKey(eventSd);
    }
}
