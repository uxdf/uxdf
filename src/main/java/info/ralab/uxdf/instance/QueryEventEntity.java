package info.ralab.uxdf.instance;

/**
 * 构建查询使用的Node实例对象，不生成UUID。不能被保存。
 */
public class QueryEventEntity extends EventEntity {
    @Override
    public String getUUID() {
        return null;
    }

    @Override
    public String generateUUID() {
        return null;
    }
}
