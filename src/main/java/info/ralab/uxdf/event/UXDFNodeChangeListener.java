package info.ralab.uxdf.event;

import info.ralab.uxdf.SdData;
import info.ralab.uxdf.instance.NodeEntity;

/**
 * Node变更监听
 */
public interface UXDFNodeChangeListener {

    /**
     * 要进行保存前。这里还未进入具体保存操作。可以变更{@link NodeEntity}的操作类型。
     *
     * @param nodeEntity
     * @param sdData
     */
    void save(NodeEntity nodeEntity, SdData sdData);

    /**
     * 新增前触发，此时还未持久化
     *
     * @param nodeEntity
     * @param sdData
     */
    void create(NodeEntity nodeEntity, SdData sdData);

    /**
     * 新增后触发，此时已经持久化
     *
     * @param nodeEntity
     * @param sdData
     */
    void created(NodeEntity nodeEntity, SdData sdData);

    /**
     * 更新前触发，此时还未持久化
     *
     * @param nodeEntity
     * @param sdData
     */
    void update(NodeEntity nodeEntity, SdData sdData);

    /**
     * 更新后触发，此时已经持久化
     *
     * @param nodeEntity
     * @param sdData
     */
    void updated(NodeEntity nodeEntity, SdData sdData);

    /**
     * 删除前触发，此时还未持久化
     *
     * @param nodeEntity
     * @param sdData
     */
    void delete(NodeEntity nodeEntity, SdData sdData);

    /**
     * 删除后触发，此时已经持久化
     *
     * @param nodeEntity
     * @param sdData
     */
    void deleted(NodeEntity nodeEntity, SdData sdData);

    /**
     * 数据查询后触发
     *
     * @param nodeEntity Node实例
     */
    void query(NodeEntity nodeEntity);

}
