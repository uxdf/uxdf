package info.ralab.uxdf.utils;

import info.ralab.uxdf.definition.SdEventDefinition;
import info.ralab.uxdf.definition.SdNodeDefinition;

/**
 * UXDF加载监听器，用于监听UXDF定义在一个{@link info.ralab.uxdf.UXDFLoader}中的变更。<br/>
 * 监听器抛出的异常，都会引起{@link info.ralab.uxdf.UXDFLoader}的变更回滚。
 */
public interface UXDFLoaderListener {

    /**
     * 添加{@link SdNodeDefinition}前触发，最终添加此方法返回的{@link SdNodeDefinition}。
     * 如果返回为NULL，表示不添加此{@link SdNodeDefinition}。
     *
     * @param nodeDefinition Node定义
     * @return 将要被添加的Node定义
     */
    SdNodeDefinition onAddNodeBefore(SdNodeDefinition nodeDefinition);

    /**
     * 添加{@link SdNodeDefinition}后触发
     *
     * @param nodeDefinition 已经添加的Node定义
     */
    void onAddNodeAfter(SdNodeDefinition nodeDefinition);

    /**
     * 修改{@link SdNodeDefinition}前触发，最终修改此方法返回的{@link SdNodeDefinition}。
     * 如果返回为NULL，表示不修改此{@link SdNodeDefinition}。
     *
     * @param nodeDefinition Node定义
     * @return 将要被修改的Node定义
     */
    SdNodeDefinition onUpdateNodeBefore(SdNodeDefinition nodeDefinition);

    /**
     * 修改{@link SdNodeDefinition}后触发
     *
     * @param nodeDefinition 已经修改的Node定义
     */
    void onUpdateNodeAfter(SdNodeDefinition nodeDefinition);

    /**
     * 删除{@link SdNodeDefinition}前触发，最终删除此方法返回的Node定义名称对应的{@link SdNodeDefinition}。
     * 如果返回为NULL，表示不删除此{@link SdNodeDefinition}。
     *
     * @param nodeName Node定义名称
     * @return 将要被删除的Node定义名称
     */
    String onRemoveNodeBefore(String nodeName);

    /**
     * 删除{@link SdNodeDefinition}后触发
     *
     * @param nodeName 被删除的Node定义名称
     */
    void onRemoveNodeAfter(String nodeName);

    /**
     * 添加{@link SdEventDefinition}前触发，最终添加此方法返回的{@link SdEventDefinition}。
     * 如果返回为NULL，表示不添加此{@link SdEventDefinition}。
     *
     * @param eventDefinition Event定义
     * @return 将要被添加的Event定义
     */
    SdEventDefinition onAddEventBefore(SdEventDefinition eventDefinition);

    /**
     * 添加{@link SdEventDefinition}后触发
     *
     * @param eventDefinition 已经添加的Event定义
     */
    void onAddEventAfter(SdEventDefinition eventDefinition);

    /**
     * 修改{@link SdEventDefinition}前触发，最终修改此方法返回的{@link SdEventDefinition}。
     * 如果返回为NULL，表示不修改此{@link SdEventDefinition}。
     *
     * @param eventDefinition Event定义
     * @return 将要被修改的Event定义
     */
    SdEventDefinition onUpdateEventBefore(SdEventDefinition eventDefinition);

    /**
     * 修改{@link SdEventDefinition}后触发
     *
     * @param eventDefinition 已经修改的Event定义
     */
    void onUpdateEventAfter(SdEventDefinition eventDefinition);

    /**
     * 删除{@link SdEventDefinition}前触发，最终删除此方法返回的Event定义名称对应的{@link SdEventDefinition}。
     * 如果返回为NULL，表示不删除此{@link SdEventDefinition}。
     *
     * @param eventNameArray Event定义名称、左Node定义名称、右Node定义名称
     * @return 将要被删除的Event定义名称
     */
    String[] onRemoveEventBefore(String[] eventNameArray);

    /**
     * 删除{@link SdEventDefinition}后触发
     *
     * @param eventNameArray 被删除的Event定义名称、左Node定义名称、右Node定义名称
     */
    void onRemoveEventAfter(String[] eventNameArray);

    /**
     * 添加{@link info.ralab.uxdf.definition.SdNodeDefinition}时，出现异常。
     *
     * @param e 异常信息
     */
    void onAddNodeError(Exception e);

    /**
     * 更新{@link info.ralab.uxdf.definition.SdNodeDefinition}时，出现异常。
     *
     * @param e 异常信息
     */
    void onUpdateNodeError(Exception e);

    /**
     * 删除{@link info.ralab.uxdf.definition.SdNodeDefinition}时，出现异常。
     *
     * @param e 异常信息
     */
    void onRemoveNodeError(Exception e);

    /**
     * 添加{@link info.ralab.uxdf.definition.SdEventDefinition}时，出现异常。
     *
     * @param e 异常信息
     */
    void onAddEventError(Exception e);

    /**
     * 修改{@link info.ralab.uxdf.definition.SdEventDefinition}时，出现异常。
     *
     * @param e 异常信息
     */
    void onUpdateEventError(Exception e);

    /**
     * 删除{@link info.ralab.uxdf.definition.SdEventDefinition}时，出现异常。
     *
     * @param e 异常信息
     */
    void onRemoveEventError(Exception e);
}
