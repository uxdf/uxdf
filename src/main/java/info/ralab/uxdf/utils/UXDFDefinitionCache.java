package info.ralab.uxdf.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.ralab.uxdf.UXDF;
import info.ralab.uxdf.UXDFException;
import info.ralab.uxdf.UXDFLoader;
import info.ralab.uxdf.definition.SdEventDefinition;
import info.ralab.uxdf.definition.SdNodeDefinition;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * UXDF定义结构缓存
 */
@Slf4j
public class UXDFDefinitionCache {

    private static final String FILE_NODES = "nodes";
    private static final String FILE_EVENTS = "events";

    /**
     * 基本UXDF结构缓存，只包含attr部分内容
     */
    @Getter
    private JSONObject CACHE_UXDF_BASE = new JSONObject();

    /**
     * 完整UXDF结构缓存，包含了impl部分内容
     */
    @Getter
    private JSONObject CACHE_UXDF_ALL = new JSONObject();

    /**
     * Node定义缓存，通过Node定义名称索引
     */
    @Getter
    private Map<String, SdNodeDefinition> CACHE_NODE = Maps.newHashMap();

    /**
     * Event定义缓存，通过Event定义名称、左Node定义名称和右Node定义名称索引
     */
    @Getter
    private Map<String, Map<String, Map<String, SdEventDefinition>>> CACHE_EVENT_LEFT = Maps.newHashMap();

    /**
     * Event定义缓存，通过Event定义名称、右Node定义名称和左Node定义名称索引
     */
    @Getter
    private Map<String, Map<String, Map<String, SdEventDefinition>>> CACHE_EVENT_RIGHT = Maps.newHashMap();

    /**
     * Node定义和Event定义关系缓存，通过Node定义名称索引
     */
    @Getter
    private Map<String, Set<SdEventDefinition>> CACHE_NODE_EVENT = Maps.newHashMap();

    /**
     * Event定义和Node定义关系缓存，通过Event定义名称索引
     */
    @Getter
    private Map<String, Set<SdNodeDefinition>> CACHE_EVENT_NODE = Maps.newHashMap();

    private List<Map> CACHE;

    public UXDFDefinitionCache() {
        this.fillCache();
    }


    /**
     * 缓存是否已经建立
     *
     * @return 是否已经创建缓存
     */
    public boolean isCached() {
        return !CACHE.stream().map(Map::isEmpty).reduce(Boolean.TRUE, (a, b) -> a && b);
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        CACHE.forEach(Map::clear);
    }

    /**
     * 序列化缓存对象到指定文件夹{@link File}下。
     *
     * @param dirPoint 序列化内容存储的文件夹路径
     * @return 序列化是否成功
     */
    public boolean serialization(final File dirPoint) {
        // 序列化文件夹不存在或不是文件夹
        if (dirPoint == null || !dirPoint.exists() || !dirPoint.isDirectory()) {
            log.error("serialize dir not exists or not is directory.");
            return false;
        }

        // 序列化所有Node
        File cacheFileNodes = new File(dirPoint, FILE_NODES);
        try (JSONWriter writer = new JSONWriter(new FileWriter(cacheFileNodes))) {
            writer.config(SerializerFeature.UseISO8601DateFormat, Boolean.TRUE);
            writer.config(SerializerFeature.DisableCircularReferenceDetect, Boolean.TRUE);
            writer.config(SerializerFeature.SortField, Boolean.TRUE);
            writer.writeObject(CACHE_NODE.values());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }

        // 序列化所有event
        File cacheFileEvents = new File(dirPoint, FILE_EVENTS);
        try (JSONWriter writer = new JSONWriter(new FileWriter(cacheFileEvents))) {
            writer.config(SerializerFeature.UseISO8601DateFormat, Boolean.TRUE);
            writer.config(SerializerFeature.DisableCircularReferenceDetect, Boolean.TRUE);
            writer.config(SerializerFeature.SortField, Boolean.TRUE);
            writer.startArray();
            CACHE_NODE_EVENT.values().forEach(sdEventDefinitions -> sdEventDefinitions.forEach(writer::writeObject));
            writer.endArray();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * 反序列化缓存对象
     *
     * @param dirPoint 序列化内容存储的文件夹路径
     * @return 反序列化是否成功
     */
    public boolean deserialization(final File dirPoint) {
        // 序列化文件夹不存在或不是文件夹
        if (dirPoint == null || !dirPoint.exists() || !dirPoint.isDirectory()) {
            log.error("serialize dir not exists or not is directory.");
            return false;
        }

        // 临时缓存集合，用于存储反序列化内容，全部读取成功后，替换已有缓存
        Map<String, SdNodeDefinition> cacheNode = Maps.newHashMap();
        Map<String, Map<String, Map<String, SdEventDefinition>>> cacheEventLeft = Maps.newHashMap();
        Map<String, Map<String, Map<String, SdEventDefinition>>> cacheEventRight = Maps.newHashMap();
        Map<String, Set<SdEventDefinition>> cacheNodeEvent = Maps.newHashMap();
        Map<String, Set<SdNodeDefinition>> cacheEventNode = Maps.newHashMap();

        // 反序列化Node文件
        File cacheFileNodes = new File(dirPoint, FILE_NODES);
        // 文件不存在 或 不是文件 或 文件不可读
        if (!cacheFileNodes.exists() || !cacheFileNodes.isFile() || !cacheFileNodes.canRead()) {
            log.error("serialize node file not exists or not file or not can read.");
            return false;
        }

        try (
                JSONReader reader = new JSONReader(new InputStreamReader(new FileInputStream(cacheFileNodes), UXDF.CHARSET))
        ) {
            reader.config(Feature.AllowISO8601DateFormat, Boolean.TRUE);
            reader.config(Feature.OrderedField, Boolean.TRUE);

            reader.startArray();
            while (reader.hasNext()) {
                this.putNodeDefinition(cacheNode, reader.readObject(SdNodeDefinition.class), Boolean.FALSE);
            }
            reader.endArray();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }


        // 反序列化Event文件
        File cacheFileEvents = new File(dirPoint, FILE_EVENTS);
        // 文件不存在 或 不是文件 或 文件不可读
        if (!cacheFileEvents.exists() || !cacheFileEvents.isFile() || !cacheFileEvents.canRead()) {
            log.error("serialize event file not exists or not file or not can read.");
            return false;
        }
        try (
                JSONReader reader = new JSONReader(new InputStreamReader(new FileInputStream(cacheFileEvents), UXDF.CHARSET))
        ) {
            reader.config(Feature.AllowISO8601DateFormat, Boolean.TRUE);
            reader.config(Feature.OrderedField, Boolean.TRUE);

            reader.startArray();
            while (reader.hasNext()) {
                this.putEventDefinition(
                        cacheNode,
                        cacheEventLeft,
                        cacheEventRight,
                        cacheNodeEvent,
                        cacheEventNode,
                        reader.readObject(SdEventDefinition.class),
                        Boolean.FALSE
                );
            }
            reader.endArray();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }

        // 替换当前缓存
        CACHE_NODE = cacheNode;
        CACHE_EVENT_LEFT = cacheEventLeft;
        CACHE_EVENT_RIGHT = cacheEventRight;
        CACHE_NODE_EVENT = cacheNodeEvent;
        CACHE_EVENT_NODE = cacheEventNode;

        this.initUXDFAll();
        this.fillCache();
        return true;
    }

    /**
     * 向当前{@link UXDFDefinitionCache}中设置{@link SdNodeDefinition}。
     * <p>
     * 以下情况会引起异常：
     * <ol>
     * <li>{@link SdNodeDefinition}为NULL。</li>
     * <li>如果不覆盖已存在的{@link SdNodeDefinition}，但添加的{@link SdNodeDefinition}已经在缓存中存在。</li>
     * <li>如果覆盖已存在的{@link SdNodeDefinition}，但添加的{@link SdNodeDefinition}已经在缓存中不存在。</li>
     * </ol>
     * </p>
     *
     * @param nodeDefinition Node定义
     * @param overwrite      是否覆盖已有定义
     */
    public void putNodeDefinition(final SdNodeDefinition nodeDefinition, final boolean overwrite) {
        this.putNodeDefinition(CACHE_NODE, nodeDefinition, overwrite);
    }

    /**
     * 向缓存集合中设置{@link SdNodeDefinition}。
     * <p>
     * 以下情况会引起异常：
     * <ol>
     * <li>{@link SdNodeDefinition}为NULL。</li>
     * <li>如果不覆盖已存在的{@link SdNodeDefinition}，但添加的{@link SdNodeDefinition}已经在缓存中存在。</li>
     * <li>如果覆盖已存在的{@link SdNodeDefinition}，但添加的{@link SdNodeDefinition}已经在缓存中不存在。</li>
     * </ol>
     * </p>
     *
     * @param cacheMap       缓存集合
     * @param nodeDefinition Node定义
     * @param overwrite      是否覆盖已有定义
     */
    private void putNodeDefinition(
            final Map<String, SdNodeDefinition> cacheMap,
            final SdNodeDefinition nodeDefinition,
            final boolean overwrite
    ) {
        // 检查是否为NULL
        if (cacheMap == null) {
            throw new UXDFException("Cache Map is null.");
        }
        if (nodeDefinition == null) {
            throw new UXDFException("Node definition is null.");
        }

        final String nodeName = nodeDefinition.getNodeName();

        // 不覆盖 并且 已存在
        if (!overwrite && cacheMap.containsKey(nodeName)) {
            throw new UXDFException(String.format("Node [%s] exist.", nodeName));
        }

        // 覆盖 并且 不存在
        if (overwrite && !cacheMap.containsKey(nodeName)) {
            throw new UXDFException(String.format("Node [%s] not exist.", nodeName));
        }

        // 加入Node缓存
        cacheMap.put(nodeName, nodeDefinition);
    }

    /**
     * 从当前{@link UXDFDefinitionCache}中移除一个{@link SdNodeDefinition}。以及相关的所有{@link SdEventDefinition}。
     *
     * @param nodeName Node定义名称
     */
    public void removeNodeDefinition(final String nodeName) {
        // 移除Node缓存
        SdNodeDefinition sdNodeDefinition = CACHE_NODE.remove(nodeName);
        // 已经被移除或者nodeName不正确，不再继续移除对应的Event
        if (sdNodeDefinition == null) {
            return;
        }

        // 从Event对应Node缓存中移除
        CACHE_EVENT_NODE.forEach((eventName, sdNodeDefinitions) -> sdNodeDefinitions.remove(sdNodeDefinition));

        // 移除所有对应的Event缓存
        Set<SdEventDefinition> sdEventDefinitions = CACHE_NODE_EVENT.remove(nodeName);
        // Node没有对应的Event
        if (sdEventDefinitions == null || sdEventDefinitions.isEmpty()) {
            return;
        }

        // 遍历被移除的Event定义，从其它缓存中同样移除
        sdEventDefinitions.forEach(sdEventDefinition -> {
            CACHE_EVENT_RIGHT.values().forEach(
                    rightMap -> rightMap.values().forEach(
                            leftMap -> leftMap.values().remove(sdEventDefinition)
                    )
            );
            CACHE_EVENT_LEFT.values().forEach(
                    leftMap -> leftMap.values().forEach(
                            rightMap -> rightMap.values().remove(sdEventDefinition)
                    )
            );
        });
    }

    /**
     * 向当前{@link UXDFDefinitionCache}中设置{@link SdEventDefinition}。
     * <p>
     * 以下情况会引起异常：
     * <ol>
     * <li>{@link SdEventDefinition}为NULL。</li>
     * <li>如果不覆盖已存在的{@link SdEventDefinition}，但添加的{@link SdEventDefinition}已经在缓存中存在。</li>
     * <li>如果覆盖已存在的{@link SdEventDefinition}，但添加的{@link SdEventDefinition}已经在缓存中不存在。</li>
     * <li>{@link SdEventDefinition#getLeftNodeName()}对应的{@link SdNodeDefinition}不存在。</li>
     * <li>{@link SdEventDefinition#getRightNodeName()}对应的{@link SdNodeDefinition}不存在。</li>
     * </ol>
     * </p>
     *
     * @param eventDefinition Event定义
     * @param overwrite       是否覆盖已有定义
     */
    public void putEventDefinition(
            final SdEventDefinition eventDefinition,
            final boolean overwrite
    ) {
        this.putEventDefinition(
                CACHE_NODE,
                CACHE_EVENT_LEFT,
                CACHE_EVENT_RIGHT,
                CACHE_NODE_EVENT,
                CACHE_EVENT_NODE,
                eventDefinition,
                overwrite
        );
    }

    /**
     * 向缓存中设置{@link SdEventDefinition}。
     * <p>
     * 以下情况会引起异常：
     * <ol>
     * <li>{@link SdEventDefinition}为NULL。</li>
     * <li>如果不覆盖已存在的{@link SdEventDefinition}，但添加的{@link SdEventDefinition}已经在缓存中存在。</li>
     * <li>如果覆盖已存在的{@link SdEventDefinition}，但添加的{@link SdEventDefinition}已经在缓存中不存在。</li>
     * <li>{@link SdEventDefinition#getLeftNodeName()}对应的{@link SdNodeDefinition}不存在。</li>
     * <li>{@link SdEventDefinition#getRightNodeName()}对应的{@link SdNodeDefinition}不存在。</li>
     * </ol>
     * </p>
     *
     * @param eventDefinition Event定义
     * @param overwrite       是否覆盖已有定义
     */
    private void putEventDefinition(
            final Map<String, SdNodeDefinition> cacheNode,
            final Map<String, Map<String, Map<String, SdEventDefinition>>> cacheEventLeft,
            final Map<String, Map<String, Map<String, SdEventDefinition>>> cacheEventRight,
            final Map<String, Set<SdEventDefinition>> cacheNodeEvent,
            final Map<String, Set<SdNodeDefinition>> cacheEventNode,
            final SdEventDefinition eventDefinition,
            final boolean overwrite
    ) {
        // 检查是否为NULL
        if (eventDefinition == null) {
            throw new UXDFException("Event definition is null.");
        }

        final String eventName = eventDefinition.getEventName();
        final String leftNodeName = eventDefinition.getLeftNodeName();
        final String rightNodeName = eventDefinition.getRightNodeName();

        // 不覆盖 并且 已存在
        if (!overwrite &&
                cacheEventLeft.containsKey(eventName) &&
                cacheEventLeft.get(eventName).containsKey(leftNodeName) &&
                cacheEventLeft.get(eventName).get(leftNodeName).containsKey(rightNodeName)
        ) {
            throw new UXDFException(String.format("Event [%s-%s>%s] exist.", eventName, leftNodeName, rightNodeName));
        }

        // 覆盖 并且 不存在
        if (overwrite &&
                (!cacheEventLeft.containsKey(eventName) ||
                        !cacheEventLeft.get(eventName).containsKey(leftNodeName) ||
                        !cacheEventLeft.get(eventName).get(leftNodeName).containsKey(rightNodeName))
        ) {
            throw new UXDFException(String.format("Event [%s-%s>%s] not exist.", eventName, leftNodeName, rightNodeName));
        }

        // 左Node定义不存在
        if (!cacheNode.containsKey(leftNodeName)) {
            throw new UXDFException(String.format("Left node [%s] not exist.", leftNodeName));
        }

        // 右Node定义不存在
        if (!cacheNode.containsKey(rightNodeName)) {
            throw new UXDFException(String.format("Right node [%s] not exist.", rightNodeName));
        }

        // 加入Node到Event缓存
        Set<SdEventDefinition> eventLeftSet = cacheNodeEvent.computeIfAbsent(leftNodeName, key -> Sets.newHashSet());
        eventLeftSet.add(eventDefinition);
        Set<SdEventDefinition> eventRightSet = cacheNodeEvent.computeIfAbsent(rightNodeName, key -> Sets.newHashSet());
        eventRightSet.add(eventDefinition);

        // 加入Event到Node缓存
        Set<SdNodeDefinition> nodeSet = cacheEventNode.computeIfAbsent(eventName, key -> Sets.newHashSet());
        nodeSet.add(cacheNode.get(leftNodeName));
        nodeSet.add(cacheNode.get(rightNodeName));

        // 加入左到右的Event缓存
        Map<String, Map<String, SdEventDefinition>> eventLeftRightMap = cacheEventLeft.computeIfAbsent(
                eventName,
                key -> Maps.newHashMap()
        );
        Map<String, SdEventDefinition> eventRightMap = eventLeftRightMap.computeIfAbsent(
                leftNodeName,
                key -> Maps.newHashMap()
        );
        eventRightMap.put(rightNodeName, eventDefinition);

        // 加入右到左的Event缓存
        Map<String, Map<String, SdEventDefinition>> eventRightLeftMap = cacheEventRight.computeIfAbsent(
                eventName,
                key -> Maps.newHashMap()
        );
        Map<String, SdEventDefinition> eventLeftMap = eventRightLeftMap.computeIfAbsent(
                rightNodeName,
                key -> Maps.newHashMap()
        );
        eventLeftMap.put(leftNodeName, eventDefinition);
    }

    /**
     * 从当前{@link UXDFLoader}中移除一个{@link SdEventDefinition}。
     *
     * @param eventNameArray Event定义名称集合，依次是eventName，leftNodeName，rightNodeName
     */
    public void removeEventDefinition(final String[] eventNameArray) {
        final String eventName = eventNameArray[0];
        final String leftNodeName = eventNameArray[1];
        final String rightNodeName = eventNameArray[2];
        // 移除Event缓存
        Set<SdEventDefinition> sdEventDefinitionSet = Sets.newHashSet();
        if (
                CACHE_EVENT_LEFT.containsKey(eventName) &&
                        CACHE_EVENT_LEFT.get(eventName).containsKey(leftNodeName) &&
                        CACHE_EVENT_LEFT.get(eventName).get(leftNodeName).containsKey(rightNodeName)
        ) {
            sdEventDefinitionSet.add(CACHE_EVENT_LEFT.get(eventName).get(leftNodeName).remove(rightNodeName));
        }

        if (
                CACHE_EVENT_RIGHT.containsKey(eventName) &&
                        CACHE_EVENT_RIGHT.get(eventName).containsKey(rightNodeName) &&
                        CACHE_EVENT_RIGHT.get(eventName).get(rightNodeName).containsKey(leftNodeName)
        ) {
            sdEventDefinitionSet.add(CACHE_EVENT_RIGHT.get(eventName).get(rightNodeName).remove(leftNodeName));
        }

        sdEventDefinitionSet.forEach(sdEventDefinition -> {
            if (sdEventDefinition == null) {
                return;
            }

            if (CACHE_NODE_EVENT.containsKey(leftNodeName)) {
                CACHE_NODE_EVENT.get(leftNodeName).remove(sdEventDefinition);
            }

            if (CACHE_NODE_EVENT.containsKey(rightNodeName)) {
                CACHE_NODE_EVENT.get(rightNodeName).remove(sdEventDefinition);
            }
        });
    }

    /**
     * 填充UXDF全集缓存
     */
    public void initUXDFAll() {
        UXDF uxdf = JSON.toJavaObject(CACHE_UXDF_BASE, UXDF.class);
        uxdf.getSd().getNode().getImpl().putAll(CACHE_NODE);
        uxdf.getSd().getEvent().getImpl().putAll(CACHE_EVENT_LEFT);
        uxdf.setData(null);
        final String jsonText = JSON.toJSONString(uxdf, SerializerFeature.DisableCircularReferenceDetect);
        CACHE_UXDF_ALL = JSON.parseObject(jsonText);
    }

    /**
     * 填充缓存集合
     */
    private void fillCache() {
        CACHE = Lists.newArrayList(
                CACHE_UXDF_BASE,
                CACHE_UXDF_ALL,
                CACHE_NODE,
                CACHE_EVENT_LEFT,
                CACHE_EVENT_RIGHT,
                CACHE_NODE_EVENT,
                CACHE_EVENT_NODE
        );
    }
}
