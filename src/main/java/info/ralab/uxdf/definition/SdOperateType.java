package info.ralab.uxdf.definition;

/**
 * SD数据操作类型
 */
public enum SdOperateType {
    /**
     * 明确新增
     */
    create,
    /**
     * 明确更新，基于__id属性
     */
    update,
    /**
     * 基于唯一属性判断，不存在新增，存在更新
     */
    createOrUpdate,
    /**
     * 基于唯一属性判断，不存在就新增
     */
    createNotExist,
    /**
     * 明确删除，基于__id属性
     */
    delete,
    /**
     * 基于所有属性查询匹配，如果有多个将按照存储排序最近一个返回
     */
    query,
    /**
     * 基于唯一属性查询匹配
     */
    match
}
