package info.ralab.uxdf.service;

import info.ralab.uxdf.SdData;
import info.ralab.uxdf.definition.SdEventDefinition;
import info.ralab.uxdf.definition.SdOperateType;
import info.ralab.uxdf.instance.EventEntity;
import info.ralab.uxdf.instance.NodeEntity;
import info.ralab.uxdf.instance.SdEntity;
import info.ralab.uxdf.model.SdDataQueryRequest;
import info.ralab.uxdf.model.SdDataQueryResult;
import info.ralab.uxdf.model.SdDataSaveResult;
import info.ralab.uxdf.utils.UXDFBinaryFileInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * UXDF数据存储服务接口
 */
public interface StorageService {

    /**
     * 初始服务
     */
    void init();

    /**
     * 销毁服务
     */
    void destory();

    /**
     * 保存UXDF数据{@link SdData}。
     * <p>
     * 根据每个{@link SdEntity#getOperate()}的类型进行新增、修改、删除操作。
     * 如果{@link SdEntity#getOperate()}为Null则不进行操作。
     * </p>
     *
     * <p>
     * 新增{@link NodeEntity}。
     * <br />
     * 新增后{@link NodeEntity#get__Id()}将获取存储后的ID。
     * </p>
     *
     * <p>
     * 更新{@link NodeEntity}。
     * <br />
     * 基于{@link NodeEntity#get__Id()}进行更新。
     * </p>
     *
     * <p>
     * 删除{@link NodeEntity}。
     * <br />
     * 基于{@link NodeEntity#get__Id()}进行删除。
     * <br />
     * 会同时删除直接关联的{@link EventEntity}。
     * <br />
     * 如果被删除的{@link EventEntity}根据{@link SdEventDefinition#getRequired()}定义，
     * 对于另外一侧的{@link NodeEntity}是必填。
     * 则会基于{@link NodeEntity#getOperateDeleteEnforce()}删除另外一侧的{@link NodeEntity}。
     * </p>
     *
     * <p>
     * 新增{@link EventEntity}。
     * <br />
     * 新增后{@link EventEntity#get__Id()}将获取存储后的ID。
     * </p>
     *
     * <p>
     * 更新{@link EventEntity}。
     * <br />
     * 基于{@link EventEntity#get__Id()}、{@link EventEntity#get__Left()}、{@link EventEntity#get__Right()}进行更新。
     * </p>
     *
     * <p>
     * 删除{@link EventEntity}。
     * <br />
     * 基于{@link EventEntity#get__Id()}、{@link EventEntity#get__Left()}、{@link EventEntity#get__Right()}进行删除。
     * <br />
     * 如果被删除的{@link EventEntity}根据{@link SdEventDefinition#getRequired()}定义，
     * 对于其两侧的{@link NodeEntity}有必填定义。
     * 则会基于{@link EventEntity#getOperateDeleteEnforce()}删除其两侧必填的{@link NodeEntity}。
     * </p>
     *
     * <p>
     * 保存结束后，返回{@link SdDataSaveResult}。
     * 从中可以获取{@link NodeEntity}和{@link EventEntity}新增、修改、删除的具体数量。
     * </p>
     *
     * @param sdData  需要保存的Sd数据
     * @param operate 默认操作类型。如果Sd实例中未指定操作类型，并且默认操作类型不为null，那么将按照默认操作类型处理
     * @param files   保存Sd实例中的二进制文件集合，下表和具体Sd实例属性值一致
     * @param sync    是否对保存完的数据，进行和存储中的同步
     * @return 保存结果
     */
    SdDataSaveResult saveData(SdData sdData, SdOperateType operate, UXDFBinaryFileInfo[] files, boolean sync);

    SdDataSaveResult saveData(SdData sdData);

    SdDataSaveResult saveAndSyncData(SdData sdData);

    NodeEntity saveNode(NodeEntity nodeEntity);

    EventEntity saveEvent(EventEntity eventEntity);

    /**
     * 使用{@link SdDataQueryRequest}查询{@link info.ralab.uxdf.UXDF}。
     *
     * @param queryRequest 查询请求
     * @return 符合查询请求的UXDF
     */
    SdDataQueryResult queryData(SdDataQueryRequest queryRequest);

    /**
     * 使用Node定义、逻辑ID获取唯一的{@link NodeEntity}。
     *
     * @param nodeName Node定义
     * @param nodeId   逻辑ID
     * @return 符合的NodeEntity
     */
    SdDataQueryResult getDataById(String nodeName, String nodeId);

    /**
     * 使用Node定义、逻辑ID获取唯一的{@link NodeEntity}。
     *
     * @param nodeName Node定义
     * @param nodeId   逻辑ID
     * @param isBlock  是否阻塞
     * @return 符合的NodeEntity
     */
    SdDataQueryResult getDataById(String nodeName, String nodeId, boolean isBlock);

    /**
     * 使用Node定义、逻辑ID获取唯一的{@link NodeEntity}。
     *
     * @param nodeName Node定义
     * @param nodeId   逻辑ID
     * @return 符合的NodeEntity
     */
    NodeEntity getNodeEntityById(String nodeName, String nodeId);

    /**
     * 使用Node定义、逻辑ID获取唯一的{@link NodeEntity}。
     *
     * @param nodeName Node定义
     * @param nodeId   逻辑ID
     * @param isBlock  是否阻塞
     * @return 符合的NodeEntity
     */
    NodeEntity getNodeEntityById(String nodeName, String nodeId, boolean isBlock);

    /**
     * 使用Node定义和唯一编码获取唯一的{@link NodeEntity}。
     *
     * @param nodeName Node定义
     * @param uuid     唯一编码
     * @return 符合的NodeEntity
     */
    NodeEntity getNodeEntityByUUID(String nodeName, String uuid);

    /**
     * 使用Node定义和唯一编码获取唯一的{@link NodeEntity}。
     *
     * @param nodeName Node定义
     * @param uuid     唯一编码
     * @param isBlock  是否阻塞
     * @return 符合的NodeEntity
     */
    NodeEntity getNodeEntityByUUID(String nodeName, String uuid, boolean isBlock);

    /**
     * 使用Event定义、逻辑ID，
     * 以及左右Node的定义、逻辑ID获取唯一的{@link EventEntity}。
     *
     * @param eventName     Event定义
     * @param eventId       逻辑ID
     * @param leftNodeName  左Node定义
     * @param leftNodeId    左逻辑ID
     * @param rightNodeName 右Node定义
     * @param rightNodeId   右逻辑ID
     * @return 符合的EventEntity
     */
    SdDataQueryResult getData(
            String eventName,
            String eventId,
            String leftNodeName,
            String leftNodeId,
            String rightNodeName,
            String rightNodeId
    );

    /**
     * 使用Event定义、逻辑ID，
     * 以及左右Node的定义、逻辑ID获取唯一的{@link EventEntity}。
     *
     * @param eventName     Event定义
     * @param eventId       逻辑ID
     * @param leftNodeName  左Node定义
     * @param leftNodeId    左逻辑ID
     * @param rightNodeName 右Node定义
     * @param rightNodeId   右逻辑ID
     * @param isBlock       是否阻塞
     * @return 符合的EventEntity
     */
    SdDataQueryResult getData(
            String eventName,
            String eventId,
            String leftNodeName,
            String leftNodeId,
            String rightNodeName,
            String rightNodeId,
            boolean isBlock
    );

    /**
     * 使用Event定义、逻辑ID，
     * 以及左右Node的定义、逻辑ID获取唯一的{@link EventEntity}。
     *
     * @param eventName     Event定义
     * @param eventId       逻辑ID
     * @param leftNodeName  左Node定义
     * @param leftNodeId    左逻辑ID
     * @param rightNodeName 右Node定义
     * @param rightNodeId   右逻辑ID
     * @return 符合的EventEntity
     */
    EventEntity getEventEntity(
            String eventName,
            String eventId,
            String leftNodeName,
            String leftNodeId,
            String rightNodeName,
            String rightNodeId
    );

    /**
     * 使用Event定义、逻辑ID，
     * 以及左右Node的定义、逻辑ID获取唯一的{@link EventEntity}。
     *
     * @param eventName     Event定义
     * @param eventId       逻辑ID
     * @param leftNodeName  左Node定义
     * @param leftNodeId    左逻辑ID
     * @param rightNodeName 右Node定义
     * @param rightNodeId   右逻辑ID
     * @param isBlock       是否阻塞
     * @return 符合的EventEntity
     */
    EventEntity getEventEntity(
            String eventName,
            String eventId,
            String leftNodeName,
            String leftNodeId,
            String rightNodeName,
            String rightNodeId,
            boolean isBlock
    );

    /**
     * 使用Event定义和唯一编码获取唯一的{@link EventEntity}。
     *
     * @param eventName Event定义
     * @param uuid      唯一编码
     * @return 符合的EventEntity
     */
    EventEntity getEventEntity(String eventName, String uuid);

    /**
     * 使用Event定义和唯一编码获取唯一的{@link EventEntity}。
     *
     * @param eventName Event定义
     * @param uuid      唯一编码
     * @param isBlock   是否阻塞
     * @return 符合的EventEntity
     */
    EventEntity getEventEntity(String eventName, String uuid, boolean isBlock);

    /**
     * 获取二进制文件流{@link java.io.InputStream}
     *
     * @param nodeName Node名称
     * @param property 属性名称
     * @param uuid     唯一标识
     * @return 二进制文件流
     */
    InputStream getUXDFBinaryFile(String nodeName, String property, String uuid);

    /**
     * 原始查询
     *
     * @param expression 原始查询表达式
     * @return 查询结果集合
     */
    List<Map<String, Object>> originalQuery(String expression);

    /**
     * 解除两个NodeEntity之间的指定类型的Event
     *
     * @param leftNode  左节点
     * @param rightNode 右节点
     * @param eventType 事件类型
     *
     * @return 实际移除的Event数量
     */
    int unlink(NodeEntity leftNode, NodeEntity rightNode, String eventType);

    /**
     * 完全清除指定Node的全部数据
     * @param nodeName 节点名称
     * @return 清除数量
     */
    int clearNode(String nodeName);
}
