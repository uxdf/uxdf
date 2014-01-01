package info.ralab.uxdf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.parser.Feature;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import info.ralab.uxdf.definition.SdEventDefinition;
import info.ralab.uxdf.definition.SdNodeDefinition;
import info.ralab.uxdf.utils.UXDFDefinitionCache;
import info.ralab.uxdf.utils.UXDFFileInfo;
import info.ralab.uxdf.utils.UXDFHelper;
import info.ralab.uxdf.utils.UXDFLoaderListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static info.ralab.uxdf.UXDF.FILE_EXTENSION;

/**
 * UXDF定义加载类，维护UXDF相关定义。处理Sd之间的继承关系等。
 *
 * @see info.ralab.uxdf.definition.SdDefinition
 * @see info.ralab.uxdf.definition.SdProperty
 * @see SdNodeDefinition
 * @see SdEventDefinition
 */
@Slf4j
public class UXDFLoader {

    /**
     * 缓存读写锁
     */
    private static final ReadWriteLock LOCK_CACHE = new ReentrantReadWriteLock();

    /**
     * 加载UXDF核心定义的相对路径
     */
    private static final String PATH_UXDF_DIR = "uxdf";
    /**
     * UXDF基本结构文件，包含了Attr部分
     */
    private static final String PATH_UXDF_FILE = PATH_UXDF_DIR + "/uxdf.json";
    /**
     * Node定义存放路径
     */
    private static final String PATH_NODE_DIR = PATH_UXDF_DIR + "/node";
    /**
     * Event定义存放路径
     */
    private static final String PATH_EVENT_DIR = PATH_UXDF_DIR + "/event";
    /**
     * 读取UXDF时的协议判断
     */
    private static final String PROTOCOL_FILE = "FILE";
    /**
     * 用户临时文件夹路径
     */
    private static final String PATH_USER_TMP = System.getProperty("java.io.tmpdir");
    /**
     * 自定义Node保存路径，默认使用用户目录下的数据路径：$HOME/.truedata/uxdf/node
     */
    @Getter
    @Setter
    private String uxdfNodeDir;

    /**
     * 自定义Event保存路径，默认使用用户目录下的数据路径：$HOME/.truedata/uxdf/event
     */
    @Getter
    @Setter
    private String uxdfEventDir;

    /**
     * 缓存集合，用于统一处理缓存清空。或判断缓存是否创建
     */
    private final static UXDFDefinitionCache CACHE = new UXDFDefinitionCache();

    /**
     * 监听器集合
     */
    private final static Set<UXDFLoaderListener> LISTENERS = Sets.newConcurrentHashSet();

    /**
     * 添加{@link UXDFLoaderListener}到{@link UXDFLoader}
     *
     * @param listener 监听器
     * @return 是否添加成功
     */
    public static boolean registerListener(final UXDFLoaderListener listener) {
        return LISTENERS.add(listener);
    }

    /**
     * 从{@link UXDFLoader}移除{@link UXDFLoaderListener}
     *
     * @param listener 监听器
     * @return 是否移除成功
     */
    public static boolean unregisterListener(final UXDFLoaderListener listener) {
        return LISTENERS.remove(listener);
    }

    /**
     * 缓存是否已经建立
     *
     * @return 是否已经创建缓存
     */
    public static boolean isCached() {
        LOCK_CACHE.readLock().lock();
        try {
            return CACHE.isCached();
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 清空{@link UXDF}中所有已缓存数据
     */
    public static void clear() {
        LOCK_CACHE.readLock().lock();
        try {
            CACHE.clear();
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 获取基本Sd定义。只包括attr部分。
     *
     * @return 一个UXDF对象副本
     */
    public static UXDF getBaseUXDF() {
        reload();
        LOCK_CACHE.readLock().lock();
        try {
            return JSON.toJavaObject(CACHE.getCACHE_UXDF_BASE(), UXDF.class);
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 获取全部SD定义。包括impl部分。
     *
     * @return 一个UXDF对象副本
     */
    public static Sd getSd() {
        reload();
        LOCK_CACHE.readLock().lock();
        try {
            Sd sd = getBaseUXDF().getSd();
            sd.getNode().setImpl(CACHE.getCACHE_NODE());
            sd.getEvent().setImpl(CACHE.getCACHE_EVENT_LEFT());
            return sd;
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 根据Node Sd名称获取Node Sd定义{@link SdNodeDefinition}
     *
     * @param nodeName Node Sd名称
     * @return Node Sd定义
     */
    public static SdNodeDefinition getNode(final String nodeName) {
        reload();
        LOCK_CACHE.readLock().lock();
        try {
            return CACHE.getCACHE_NODE().get(nodeName);
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 获取全部Node Sd定义{@link SdNodeDefinition}
     *
     * @return Node Sd定义集合
     */
    public static Collection<SdNodeDefinition> getNodes() {
        LOCK_CACHE.readLock().lock();
        try {
            return CACHE.getCACHE_NODE().values();
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 获取Event名称对应的所有{@link SdEventDefinition}集合
     *
     * @param eventName Event名称
     * @return Event定义集合
     */
    public static Map<String, Map<String, SdEventDefinition>> getEvent(final String eventName) {
        reload();
        LOCK_CACHE.readLock().lock();
        try {
            return CACHE.getCACHE_EVENT_LEFT().get(eventName);
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 获取所有{@link SdEventDefinition}
     *
     * @return Event定义集合
     */
    public static Map<String, Map<String, Map<String, SdEventDefinition>>> getEvents() {
        reload();
        LOCK_CACHE.readLock().lock();
        try {
            return CACHE.getCACHE_EVENT_LEFT();
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 获取符合Event Sd定义名称、左Node Sd定义名称和右Node Sd定义名的{@link SdEventDefinition}
     *
     * @param eventName     Event Sd定义名称
     * @param leftNodeName  左Node Sd定义名
     * @param rightNodeName 右Node Sd定义名称
     * @return Event定义
     */
    public static SdEventDefinition getEvent(final String eventName, final String leftNodeName, final String rightNodeName) {
        reload();
        LOCK_CACHE.readLock().lock();
        try {
            if (eventName == null) {
                return null;
            }

            Map<String, Map<String, SdEventDefinition>> leftEvent = CACHE.getCACHE_EVENT_LEFT().get(eventName);
            if (leftEvent == null) {
                return null;
            }
            Map<String, SdEventDefinition> rightEvent = leftEvent.get(leftNodeName);
            if (rightEvent == null) {
                return null;
            }
            return rightEvent.get(rightNodeName);
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 根据Node名称获取关联的Event集合
     *
     * @param nodeName Node 名称
     * @return 关联的Event集合
     */
    public static Set<SdEventDefinition> getEventsByNodeName(final String nodeName) {
        reload();
        LOCK_CACHE.readLock().lock();
        try {
            return CACHE.getCACHE_NODE_EVENT().getOrDefault(nodeName, Sets.newHashSet());
        } finally {
            LOCK_CACHE.readLock().unlock();
        }
    }

    /**
     * 重新加载Sd定义，如果已经加载过。则不重新加载。<br />
     * 如果重新加载，会对所有缓存的读写请求锁定。直到加载完成。
     */
    public static void reload() {
        // 缓存存在不重新加载
        if (isCached()) {
            return;
        }
        reloadForced();
    }

    /**
     * 清除已有缓存，重新加载Sd定义。会对所有缓存的读写请求锁定。直到加载完成。
     */
    public static void reloadForced() {
        LOCK_CACHE.writeLock().lock();

        try {
            // 清除缓存
            CACHE.clear();

            // 加载UXDF基本定义文件
            loadBaseUXDF();

            // 加载UXDF Node定义文件
            loadNodeDefinition();

            // 加载所有的Event
            loadEventDefinition();

            // 生成UXDF完整缓存
            CACHE.initUXDFAll();
        } catch (IOException e) {
            throw new UXDFException(e);
        } finally {
            LOCK_CACHE.writeLock().unlock();
        }

    }

    /**
     * 向当前{@link UXDFLoader}中添加{@link SdNodeDefinition}。
     * <p>
     * 此操作会对所有缓存读写加锁。<br />
     * 如果出现添加的{@link SdNodeDefinition}已经在缓存中存在，则会引起异常。
     * </p>
     *
     * @param nodeDefinitions Node定义集合
     */
    public static void addNodeDefinition(final List<SdNodeDefinition> nodeDefinitions) {
        LOCK_CACHE.writeLock().lock();
        // 创建还原点
        final String point = openRestorePoint();
        try {
            if (nodeDefinitions == null || nodeDefinitions.isEmpty()) {
                return;
            }

            // 修改前事件通知
            // 变更集合，用于存放事件处理后的定义
            List<SdNodeDefinition> handledDefinitions = Lists.newArrayList();
            nodeDefinitions.forEach(sdNodeDefinition -> {
                // 创建监听器处理的定义
                SdNodeDefinition listenerHandleDefinition = sdNodeDefinition;
                // 遍历监听器开始通知
                for (UXDFLoaderListener listener : LISTENERS) {
                    // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                    if (listener == null) {
                        continue;
                    }
                    listenerHandleDefinition = listener.onAddNodeBefore(listenerHandleDefinition);
                    // 如果监听器返回了NULL，则跳出监听器循环。
                    if (listenerHandleDefinition == null) {
                        break;
                    }
                }
                // 监听器处理后不为NULL，加入变更集合
                if (listenerHandleDefinition != null) {
                    handledDefinitions.add(listenerHandleDefinition);
                }
            });

            // 执行变更
            handledDefinitions.forEach(sdNodeDefinition -> CACHE.putNodeDefinition(sdNodeDefinition, Boolean.FALSE));
            // 刷新Node缓存
            flushNodeCache();

            // 修改后事件通知
            handledDefinitions.forEach(sdNodeDefinition -> LISTENERS.forEach(uxdfLoaderListener -> {
                // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                if (uxdfLoaderListener != null) {
                    uxdfLoaderListener.onAddNodeAfter(sdNodeDefinition);
                }
            }));

            // 移除还原点
            removeRestorePoint(point);
        } catch (Exception e) {
            // 异常通知
            LISTENERS.forEach(uxdfLoaderListener -> {
                try {
                    uxdfLoaderListener.onAddNodeError(e);
                } catch (Exception listenerError) {
                    log.error(listenerError.getLocalizedMessage(), listenerError);
                }
            });
            // 恢复还原点
            if (rollbackRestorePoint(point)) {
                throw new UXDFException(e);
            } else {
                throw new UXDFException("rollback uxdf loader cache fail.", e);
            }
        } finally {
            LOCK_CACHE.writeLock().unlock();
        }
    }

    /**
     * 更新当前{@link UXDFLoader}中{@link SdNodeDefinition}。
     * <p>
     * 此操作会对所有缓存读写加锁。<br />
     * 如果出现需要更新的{@link SdNodeDefinition}未在缓存中存在，则会引起异常。
     * </p>
     *
     * @param nodeDefinitions Node定义集合
     */
    public static void updateNodeDefinition(final List<SdNodeDefinition> nodeDefinitions) {
        LOCK_CACHE.writeLock().lock();
        final String point = openRestorePoint();
        try {
            if (nodeDefinitions == null || nodeDefinitions.isEmpty()) {
                return;
            }

            // 修改前事件通知
            // 变更集合，用于存放事件处理后的定义
            List<SdNodeDefinition> handledDefinitions = Lists.newArrayList();
            nodeDefinitions.forEach(sdNodeDefinition -> {
                // 创建监听器处理的定义
                SdNodeDefinition listenerHandleDefinition = sdNodeDefinition;
                // 遍历监听器开始通知
                for (UXDFLoaderListener listener : LISTENERS) {
                    // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                    if (listener == null) {
                        continue;
                    }
                    listenerHandleDefinition = listener.onUpdateNodeBefore(listenerHandleDefinition);
                    // 如果监听器返回了NULL，则跳出监听器循环。
                    if (listenerHandleDefinition == null) {
                        break;
                    }
                }
                // 监听器处理后不为NULL，加入变更集合
                if (listenerHandleDefinition != null) {
                    handledDefinitions.add(listenerHandleDefinition);
                }
            });

            // 执行变更
            handledDefinitions.forEach(sdNodeDefinition -> CACHE.putNodeDefinition(sdNodeDefinition, Boolean.TRUE));
            // 刷新Node缓存
            flushNodeCache();

            // 修改后事件通知
            handledDefinitions.forEach(sdNodeDefinition -> LISTENERS.forEach(uxdfLoaderListener -> {
                // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                if (uxdfLoaderListener != null) {
                    uxdfLoaderListener.onUpdateNodeAfter(sdNodeDefinition);
                }
            }));

            // 移除还原点
            removeRestorePoint(point);
        } catch (Exception e) {
            // 异常通知
            LISTENERS.forEach(uxdfLoaderListener -> {
                try {
                    uxdfLoaderListener.onUpdateNodeError(e);
                } catch (Exception listenerError) {
                    log.error(listenerError.getLocalizedMessage(), listenerError);
                }
            });
            // 恢复还原点
            if (rollbackRestorePoint(point)) {
                throw new UXDFException(e);
            } else {
                throw new UXDFException("rollback uxdf loader cache fail.", e);
            }
        } finally {
            LOCK_CACHE.writeLock().unlock();
        }
    }

    /**
     * 删除当前{@link UXDFLoader}中{@link SdNodeDefinition}。
     * <p>
     * 此操作会对所有缓存读写加锁。
     * </p>
     *
     * @param nodeNames Node定义名称集合
     */
    public static void removeNodeDefinition(final List<String> nodeNames) {
        LOCK_CACHE.writeLock().lock();
        final String point = openRestorePoint();
        try {
            if (nodeNames == null || nodeNames.isEmpty()) {
                return;
            }

            // 修改前事件通知
            // 变更集合，用于存放事件处理后的定义
            List<String> handledNames = Lists.newArrayList();
            nodeNames.forEach(nodeName -> {
                // 创建监听器处理的定义
                String listenerHandleName = nodeName;
                // 遍历监听器开始通知
                for (UXDFLoaderListener listener : LISTENERS) {
                    // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                    if (listener == null) {
                        continue;
                    }
                    listenerHandleName = listener.onRemoveNodeBefore(listenerHandleName);
                    // 如果监听器返回了NULL，则跳出监听器循环。
                    if (listenerHandleName == null) {
                        break;
                    }
                }
                // 监听器处理后不为NULL，加入变更集合
                if (listenerHandleName != null) {
                    handledNames.add(listenerHandleName);
                }
            });

            // 执行变更
            handledNames.forEach(CACHE::removeNodeDefinition);
            // 刷新Node缓存
            flushNodeCache();

            // 修改后事件通知
            handledNames.forEach(nodeName -> LISTENERS.forEach(uxdfLoaderListener -> {
                // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                if (uxdfLoaderListener != null) {
                    uxdfLoaderListener.onRemoveNodeAfter(nodeName);
                }
            }));

            // 移除还原点
            removeRestorePoint(point);
        } catch (Exception e) {
            // 异常通知
            LISTENERS.forEach(uxdfLoaderListener -> {
                try {
                    uxdfLoaderListener.onRemoveNodeError(e);
                } catch (Exception listenerError) {
                    log.error(listenerError.getLocalizedMessage(), listenerError);
                }
            });
            // 恢复还原点
            if (rollbackRestorePoint(point)) {
                throw new UXDFException(e);
            } else {
                throw new UXDFException("rollback uxdf loader cache fail.", e);
            }
        } finally {
            LOCK_CACHE.writeLock().unlock();
        }
    }

    /**
     * 向当前{@link UXDFLoader}中添加{@link SdEventDefinition}。
     * <p>
     * 此操作会对所有缓存读写加锁。<br />
     *
     * <ol>
     * <li>如果出现添加的{@link SdEventDefinition}已经在缓存中存在，则会引起异常。</li>
     * <li>{@link SdEventDefinition#getLeftNodeName()}对应的{@link SdNodeDefinition}不存在。</li>
     * <li>{@link SdEventDefinition#getRightNodeName()}对应的{@link SdNodeDefinition}不存在。</li>
     * </ol>
     * </p>
     *
     * @param eventDefinitions Node定义集合
     */
    public static void addEventDefinition(final List<SdEventDefinition> eventDefinitions) {
        LOCK_CACHE.writeLock().lock();
        final String point = openRestorePoint();
        try {
            if (eventDefinitions == null || eventDefinitions.isEmpty()) {
                return;
            }

            // 修改前事件通知
            // 变更集合，用于存放事件处理后的定义
            List<SdEventDefinition> handledDefinitions = Lists.newArrayList();
            eventDefinitions.forEach(sdEventDefinition -> {
                // 创建监听器处理的定义
                SdEventDefinition listenerHandleDefinition = sdEventDefinition;
                // 遍历监听器开始通知
                for (UXDFLoaderListener listener : LISTENERS) {
                    // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                    if (listener == null) {
                        continue;
                    }
                    listenerHandleDefinition = listener.onAddEventBefore(listenerHandleDefinition);
                    // 如果监听器返回了NULL，则跳出监听器循环。
                    if (listenerHandleDefinition == null) {
                        break;
                    }
                }
                // 监听器处理后不为NULL，加入变更集合
                if (listenerHandleDefinition != null) {
                    handledDefinitions.add(listenerHandleDefinition);
                }
            });

            // 执行变更
            handledDefinitions.forEach(sdEventDefinition -> CACHE.putEventDefinition(sdEventDefinition, Boolean.FALSE));

            // 修改后事件通知
            handledDefinitions.forEach(sdEventDefinition -> LISTENERS.forEach(uxdfLoaderListener -> {
                // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                if (uxdfLoaderListener != null) {
                    uxdfLoaderListener.onAddEventAfter(sdEventDefinition);
                }
            }));

            // 移除还原点
            removeRestorePoint(point);
        } catch (Exception e) {
            // 异常通知
            LISTENERS.forEach(uxdfLoaderListener -> {
                try {
                    uxdfLoaderListener.onAddEventError(e);
                } catch (Exception listenerError) {
                    log.error(listenerError.getLocalizedMessage(), listenerError);
                }
            });
            // 恢复还原点
            if (rollbackRestorePoint(point)) {
                throw new UXDFException(e);
            } else {
                throw new UXDFException("rollback uxdf loader cache fail.", e);
            }
        } finally {
            LOCK_CACHE.writeLock().unlock();
        }
    }

    /**
     * 更新当前{@link UXDFLoader}中{@link SdEventDefinition}。
     * <p>
     * 此操作会对所有缓存读写加锁。<br />
     * <ol>
     * <li>如果出现需要更新的{@link SdEventDefinition}未在缓存中存在，则会引起异常。</li>
     * <li>{@link SdEventDefinition#getLeftNodeName()}对应的{@link SdNodeDefinition}不存在。</li>
     * <li>{@link SdEventDefinition#getRightNodeName()}对应的{@link SdNodeDefinition}不存在。</li>
     * </ol>
     *
     * </p>
     *
     * @param eventDefinitions Event定义集合
     */
    public static void updateEventDefinition(final List<SdEventDefinition> eventDefinitions) {
        LOCK_CACHE.writeLock().lock();
        final String point = openRestorePoint();
        try {
            if (eventDefinitions == null || eventDefinitions.isEmpty()) {
                return;
            }

            // 修改前事件通知
            // 变更集合，用于存放事件处理后的定义
            List<SdEventDefinition> handledDefinitions = Lists.newArrayList();
            eventDefinitions.forEach(sdEventDefinition -> {
                // 创建监听器处理的定义
                SdEventDefinition listenerHandleDefinition = sdEventDefinition;
                // 遍历监听器开始通知
                for (UXDFLoaderListener listener : LISTENERS) {
                    // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                    if (listener == null) {
                        continue;
                    }
                    listenerHandleDefinition = listener.onUpdateEventBefore(listenerHandleDefinition);
                    // 如果监听器返回了NULL，则跳出监听器循环。
                    if (listenerHandleDefinition == null) {
                        break;
                    }
                }
                // 监听器处理后不为NULL，加入变更集合
                if (listenerHandleDefinition != null) {
                    handledDefinitions.add(listenerHandleDefinition);
                }
            });

            // 执行变更
            eventDefinitions.forEach(sdEventDefinition -> CACHE.putEventDefinition(sdEventDefinition, Boolean.TRUE));

            // 修改后事件通知
            handledDefinitions.forEach(sdEventDefinition -> LISTENERS.forEach(uxdfLoaderListener -> {
                // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                if (uxdfLoaderListener != null) {
                    uxdfLoaderListener.onUpdateEventAfter(sdEventDefinition);
                }
            }));

            // 移除还原点
            removeRestorePoint(point);
        } catch (Exception e) {
            // 异常通知
            LISTENERS.forEach(uxdfLoaderListener -> {
                try {
                    uxdfLoaderListener.onUpdateEventError(e);
                } catch (Exception listenerError) {
                    log.error(listenerError.getLocalizedMessage(), listenerError);
                }
            });
            // 恢复还原点
            if (rollbackRestorePoint(point)) {
                throw new UXDFException(e);
            } else {
                throw new UXDFException("rollback uxdf loader cache fail.", e);
            }
        } finally {
            LOCK_CACHE.writeLock().unlock();
        }
    }

    /**
     * 删除当前{@link UXDFLoader}中{@link SdEventDefinition}。
     * <p>
     * 此操作会对所有缓存读写加锁。
     * </p>
     *
     * @param eventNames Event定义名称集合，每一项名称顺序为eventName，leftNodeName，rightNodeName。
     */
    public static void removeEventDefinition(final List<String[]> eventNames) {
        LOCK_CACHE.writeLock().lock();
        final String point = openRestorePoint();
        try {
            if (eventNames == null || eventNames.isEmpty()) {
                return;
            }

            // 修改前事件通知
            // 变更集合，用于存放事件处理后的定义
            List<String[]> handledNames = Lists.newArrayList();
            eventNames.forEach(eventNameArray -> {
                // 创建监听器处理的定义
                String[] listenerHandleName = eventNameArray;
                // 遍历监听器开始通知
                for (UXDFLoaderListener listener : LISTENERS) {
                    // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                    if (listener == null) {
                        continue;
                    }
                    listenerHandleName = listener.onRemoveEventBefore(listenerHandleName);
                    // 如果监听器返回了NULL，则跳出监听器循环。
                    if (listenerHandleName == null) {
                        break;
                    }
                }
                // 监听器处理后不为NULL，加入变更集合
                if (listenerHandleName != null) {
                    handledNames.add(listenerHandleName);
                }
            });

            // 执行变更
            handledNames.forEach(CACHE::removeEventDefinition);

            // 修改后事件通知
            handledNames.forEach(eventNameArray -> LISTENERS.forEach(uxdfLoaderListener -> {
                // 跳过空监听器ε=ε=ε=(~￣▽￣)~
                if (uxdfLoaderListener != null) {
                    uxdfLoaderListener.onRemoveEventAfter(eventNameArray);
                }
            }));

            // 移除还原点
            removeRestorePoint(point);
        } catch (Exception e) {
            // 异常通知
            LISTENERS.forEach(uxdfLoaderListener -> {
                try {
                    uxdfLoaderListener.onRemoveEventError(e);
                } catch (Exception listenerError) {
                    log.error(listenerError.getLocalizedMessage(), listenerError);
                }
            });
            // 恢复还原点
            if (rollbackRestorePoint(point)) {
                throw new UXDFException(e);
            } else {
                throw new UXDFException("rollback uxdf loader cache fail.", e);
            }
        } finally {
            LOCK_CACHE.writeLock().unlock();
        }
    }

    /**
     * 获取完整UXDF定义的JSON字符串
     *
     * @return UXDF定义的JSON字符串
     */
    public static String getAllUxdfJSON() {
        return CACHE.getCACHE_UXDF_ALL().toJSONString();
    }


    /**
     * 通过约定的class path加载{@link SdEventDefinition}定义
     */
    private static void loadEventDefinition() throws IOException {
        // 获取当前进程下，所有约定的Event Sd定义存放路径
        final List<UXDFFileInfo> uxdfFileInfoList = loadUxdfFileInfo(PATH_EVENT_DIR);

        // 遍历所有加载到的文件信息，读取文件内容
        for (UXDFFileInfo eventFileInfo : uxdfFileInfoList) {
            // Event定义名称
            String eventName = eventFileInfo.getName();
            // 读取定义
            try (JSONReader reader = new JSONReader(new InputStreamReader(eventFileInfo.getInputStream(), UXDF.CHARSET))) {
                JSONObject eventLeft = reader.readObject(JSONObject.class);
                // 遍历左Node对应的所有右Node
                eventLeft.keySet().forEach((leftNodeName) -> {
                    JSONObject eventRight = eventLeft.getJSONObject(leftNodeName);
                    // 遍历所有的右Node对应的Event具体定义
                    eventRight.keySet().forEach((rightNodeName) -> {
                        // 创建Event定义
                        SdEventDefinition eventDefinition = eventRight.getJSONObject(rightNodeName).toJavaObject(SdEventDefinition.class);
                        eventDefinition.setLeftNodeName(leftNodeName);
                        eventDefinition.setRightNodeName(rightNodeName);
                        eventDefinition.setEventName(eventName);

                        CACHE.putEventDefinition(eventDefinition, Boolean.FALSE);
                    });
                });
            }
        }
    }

    /**
     * 通过约定的class path加载{@link SdNodeDefinition}定义
     */
    private static void loadNodeDefinition() throws IOException {
        // 获取当前进程下，所有约定的Node Sd定义存放路径
        final List<UXDFFileInfo> uxdfFileInfoList = loadUxdfFileInfo(PATH_NODE_DIR);

        // 遍历Node定义文件集合，读取定义内容
        for (UXDFFileInfo nodeFileWrapper : uxdfFileInfoList) {
            // Node定义名称
            final String nodeName = nodeFileWrapper.getName();
            try (
                    JSONReader reader = new JSONReader(new InputStreamReader(
                            nodeFileWrapper.getInputStream(), UXDF.CHARSET
                    ), Feature.OrderedField)
            ) {
                // 根据文件创建Node定义，加入缓存
                SdNodeDefinition nodeDefinition = reader.readObject(SdNodeDefinition.class);
                nodeDefinition.setNodeName(nodeName);
                CACHE.putNodeDefinition(nodeDefinition, Boolean.FALSE);
            }
        }
        // 刷新Node缓存
        flushNodeCache();
    }

    /**
     * 从约定class path 路径下加载所有uxdf定义文件
     *
     * @param classPath 加载路径
     * @throws IOException 异常
     */
    private static List<UXDFFileInfo> loadUxdfFileInfo(String classPath) throws IOException {
        List<UXDFFileInfo> uxdfFileInfoList = Lists.newArrayList();

        Enumeration<URL> nodeDirPaths = Thread.currentThread().getContextClassLoader().getResources(classPath);
        while (nodeDirPaths.hasMoreElements()) {
            final String nodeDirPath = nodeDirPaths.nextElement().getPath();
            // 加载Event定义文件信息
            loadInnerUXDFInfoFile(uxdfFileInfoList, nodeDirPath, classPath);
        }

        return uxdfFileInfoList;
    }

    /**
     * 刷新Node缓存，重建继承关系和唯一约束
     */
    private static void flushNodeCache() {
        CACHE.getCACHE_NODE().forEach((nodeName, node) -> {
            // 处理继承
            String[] extendArray = node.getExtend();
            if (extendArray != null && extendArray.length > 0) {
                // 倒序继承顺序从后向前继承
                List<String> extendList = Arrays.asList(extendArray);
                Collections.reverse(extendList);
                // 遍历所有继承的父Node，将继承的属性添加到子Node
                extendList.forEach((parentNodeName) -> {
                    SdNodeDefinition parentNode = CACHE.getCACHE_NODE().get(parentNodeName);
                    parentNode.getProp().forEach((parentPropName, parentProp) -> {
                        // 子未实现，则使用父
                        if (!node.getProp().containsKey(parentPropName)) {
                            node.getProp().put(parentPropName, parentProp);
                        }
                    });
                });
            }


            // 处理唯一约束
            String[] uniqueIndex = node.getUniqueIndex();
            if (uniqueIndex != null && uniqueIndex.length > 0) {
                // 加入版本库和分支作为唯一约束
                Set<String> uniqueIndexList = Sets.newHashSet(uniqueIndex);
                node.setUniqueIndex(uniqueIndexList.toArray(new String[]{}));
            }

        });
    }

    /**
     * 从约定的class path中加载UXDF基本定义{@link Sd}。
     * 其中只包括Node和Event的attr部分。
     */
    private static void loadBaseUXDF() throws UnsupportedEncodingException {
        JSONObject uxdfJSON;
        try (
                JSONReader reader = new JSONReader(
                        new InputStreamReader(
                                Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_UXDF_FILE),
                                UXDF.CHARSET
                        ),
                        Feature.OrderedField
                )
        ) {
            uxdfJSON = reader.readObject(JSONObject.class);
        }
        // 替换缓存
        CACHE.getCACHE_UXDF_BASE().putAll(uxdfJSON);
    }

    /**
     * 加载UXDF定义，{@link SdNodeDefinition}或{@link SdEventDefinition}。
     * <p>
     * 在<b>UXDF定义存放目录路径</b>中查找所有匹配<b>UXDF定义类型路径，Node路径或Event路径</b>的位置。
     * 从其中读取定义文件创建{@link UXDFFileInfo}，并加入<b>UXDF定义文件信息集合</b>中。
     * </p>
     *
     * @param uxdfFileInfoList UXDF定义文件信息集合
     * @param uxdfFileDirPath  UXDF定义存放目录路径
     * @param uxdfTypePath     UXDF定义类型路径，Node路径或Event路径
     * @throws IOException 定义文件读取异常
     */
    private static void loadInnerUXDFInfoFile(
            final List<UXDFFileInfo> uxdfFileInfoList,
            final String uxdfFileDirPath,
            final String uxdfTypePath
    ) throws IOException {
        log.debug("uxdf file dir path: {}", uxdfFileDirPath);
        log.debug("uxdf type path: {}", uxdfTypePath);
        if (uxdfFileDirPath.contains("!")) { // 在War包或Jar包中
            log.debug("uxdf dir path in war.");
            // 截取jar包或war包路径
            String jarFilePath = uxdfFileDirPath.substring(0, uxdfFileDirPath.lastIndexOf("!"));
            // 如果路径开头有协议部分，去掉协议部分只保留文件路径。
            if (jarFilePath.toUpperCase().startsWith(PROTOCOL_FILE)) {
                jarFilePath = new URL(jarFilePath).getPath();
            }
            log.debug("war file path: {}", jarFilePath);
            // 创建Jar文件
            JarFile jarFile = new JarFile(jarFilePath);
            // 遍历jar文件中所有资源
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                final String jarPath = jarEntry.getName();
                log.debug("uxdf file path: {}", jarPath);
                // 根据路径匹配和文件后缀读取UXDF定义文件
                if (jarPath.startsWith(uxdfTypePath) && jarPath.endsWith(FILE_EXTENSION)) {
                    String path = jarFilePath + "!" + jarPath;
                    String name = UXDFHelper.getNameWithoutExtension(path);
                    InputStream inputStream = jarFile.getInputStream(jarEntry);
                    uxdfFileInfoList.add(new UXDFFileInfo(name, inputStream, path));
                }
            }
        } else { // 在文件系统中

            File uxdfDir = new File(uxdfFileDirPath);
            File[] innerEventFiles = uxdfDir.listFiles(jsonFilenameFilter);
            if (innerEventFiles != null) {
                for (File innerEventFile : innerEventFiles) {
                    String path = innerEventFile.getAbsolutePath();
                    String name = UXDFHelper.getNameWithoutExtension(path);
                    InputStream inputStream = new FileInputStream(innerEventFile);
                    uxdfFileInfoList.add(new UXDFFileInfo(name, inputStream, path));
                }
            }
        }
    }

    /**
     * JSON文件过滤器
     */
    private static FilenameFilter jsonFilenameFilter = (dir, name) -> {
        // 只返回JSON文件
        return name.endsWith(FILE_EXTENSION);
    };


    /**
     * 创建缓存还原点
     *
     * @return 还原点文件名称
     */
    private static String openRestorePoint() {
        final String point = "." + UUID.randomUUID().toString();

        File dirPoint = new File(PATH_USER_TMP, point);
        // 检查还原点是否已经存在
        if (dirPoint.exists()) {
            throw new UXDFException(String.format("Restore point [%s] exist.", dirPoint.getAbsolutePath()));
        }
        // 创建还原点文件夹
        if (!dirPoint.mkdirs()) {
            throw new UXDFException(String.format("Restore point [%s] create fail.", dirPoint.getAbsolutePath()));
        }

        // 创建还原点内容
        if (!CACHE.serialization(dirPoint)) {
            throw new UXDFException(String.format("Restore point [%s] serialize fail.", dirPoint.getAbsolutePath()));
        }

        return point;
    }

    /**
     * 移除还原点
     *
     * @param point 还原点文件名称
     */
    private static void removeRestorePoint(final String point) {
        File dirPoint = new File(PATH_USER_TMP, point);

        // 文件存在，但是删除失败
        if (dirPoint.exists() && !UXDFHelper.deleteDir(dirPoint)) {
            throw new UXDFException(String.format("Restore point [%s] remove fail.", dirPoint.getAbsolutePath()));
        }
    }

    /**
     * 恢复还原点
     *
     * @param point 还原点文件名称
     * @return 回滚是否成功
     */
    private static boolean rollbackRestorePoint(final String point) {
        File dirPoint = new File(PATH_USER_TMP, point);

        return CACHE.deserialization(dirPoint);
    }
}
