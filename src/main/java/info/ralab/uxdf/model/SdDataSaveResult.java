package info.ralab.uxdf.model;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 保存{@link info.ralab.uxdf.SdData}的结果。
 */
@Data
public class SdDataSaveResult {
    /**
     * {@link info.ralab.uxdf.instance.NodeEntity}创建数量
     */
    private AtomicInteger nodeCreateNum = new AtomicInteger();
    /**
     * {@link info.ralab.uxdf.instance.NodeEntity}修改数量
     */
    private AtomicInteger nodeUpdateNum = new AtomicInteger();
    /**
     * {@link info.ralab.uxdf.instance.NodeEntity}删除数量
     */
    private AtomicInteger nodeDeleteNum = new AtomicInteger();
    /**
     * {@link info.ralab.uxdf.instance.EventEntity}创建数量
     */
    private AtomicInteger eventCreateNum = new AtomicInteger();
    /**
     * {@link info.ralab.uxdf.instance.EventEntity}修改数量
     */
    private AtomicInteger eventUpdateNum = new AtomicInteger();
    /**
     * {@link info.ralab.uxdf.instance.EventEntity}删除数量
     */
    private AtomicInteger eventDeleteNum = new AtomicInteger();
}
