package info.ralab.uxdf.definition;

import com.google.common.collect.Maps;
import info.ralab.uxdf.UXDFException;
import lombok.Data;

import java.util.Map;

/**
 * SD Event结构定义
 */
@Data
public class SdEvent {

    private Map<String, SdProperty> attr = Maps.newLinkedHashMap();
    private Map<String, Map<String, Map<String, SdEventDefinition>>> impl = Maps.newLinkedHashMap();

    public void setImpl(final Map<String, Map<String, Map<String, SdEventDefinition>>> impl) {
        this.impl.clear();
        if (impl == null || impl.isEmpty()) {
            return;
        }
        impl.forEach((eventName, leftMap) -> {
            leftMap.forEach((leftNodeName, rightMap) -> {
                rightMap.forEach((rightNodeName, sdEvent) -> {
                    sdEvent.setEventName(eventName);
                    sdEvent.setLeftNodeName(leftNodeName);
                    sdEvent.setRightNodeName(rightNodeName);
                    this.put(sdEvent);
                });
            });
        });
    }

    /**
     * 判断SdEvent定义是否存在
     *
     * @param eventName
     * @param leftNodeName
     * @param rightNodeName
     * @return
     */
    public boolean contains(final String eventName, final String leftNodeName, final String rightNodeName) {
        return this.impl.containsKey(eventName) &&
                this.impl.get(eventName).containsKey(leftNodeName) &&
                this.impl.get(eventName).get(leftNodeName).containsKey(rightNodeName);
    }

    /**
     * 判断SdEvent定义是否存在
     *
     * @param sdEventDefinition
     * @return
     */
    public boolean contains(final SdEventDefinition sdEventDefinition) {
        String eventName = sdEventDefinition.getEventName();
        String leftNodeName = sdEventDefinition.getLeftNodeName();
        String rightNodeName = sdEventDefinition.getRightNodeName();
        return this.contains(eventName, leftNodeName, rightNodeName);
    }

    /**
     * 添加Event定义
     *
     * @param sdEventDefinition
     */
    public void put(final SdEventDefinition sdEventDefinition) {
        this.put(sdEventDefinition, false);
    }

    /**
     * 添加Event定义
     *
     * @param sdEventDefinition
     */
    public void put(final SdEventDefinition sdEventDefinition, final boolean override) {
        String eventName = sdEventDefinition.getEventName();
        String leftNodeName = sdEventDefinition.getLeftNodeName();
        String rightNodeName = sdEventDefinition.getRightNodeName();

        if (!this.impl.containsKey(eventName)) {
            this.impl.put(eventName, Maps.newHashMap());
        }
        if (!this.impl.get(eventName).containsKey(leftNodeName)) {
            this.impl.get(eventName).put(leftNodeName, Maps.newHashMap());
        }
        if (this.impl.get(eventName).get(leftNodeName).containsKey(rightNodeName)) {
            if (override) {
                this.impl.get(eventName).get(leftNodeName).put(rightNodeName, sdEventDefinition);
            } else {
                // 当前Node中的两个属性引用了同一个Node
                throw new UXDFException(
                        String.format("Event %s-%s>%s appear twice.", leftNodeName, eventName, rightNodeName)
                );
            }
        } else {
            this.impl.get(eventName).get(leftNodeName).put(rightNodeName, sdEventDefinition);
        }
    }

    public void merge(final SdEvent event) {
        if (event == null) {
            return;
        }
        if (event.getAttr() != null) {
            this.attr.putAll(event.getAttr());
        }
        if (event.getImpl() != null) {
            if (this.impl.isEmpty()) {
                this.impl.putAll(event.getImpl());
            } else {
                event.getImpl().forEach((eventName, leftEvent) -> {
                    if (this.impl.containsKey(eventName)) {
                        Map<String, Map<String, SdEventDefinition>> thisLeft = this.impl.get(eventName);
                        if (thisLeft.isEmpty()) {
                            thisLeft.putAll(leftEvent);
                        } else {
                            leftEvent.forEach((leftNodeName, rightEvent) -> {
                                if (thisLeft.containsKey(leftNodeName)) {
                                    Map<String, SdEventDefinition> thisRight = thisLeft.get(leftNodeName);
                                    thisRight.putAll(rightEvent);
                                } else {
                                    thisLeft.put(leftNodeName, rightEvent);
                                }
                            });
                        }
                    } else {
                        this.impl.put(eventName, leftEvent);
                    }
                });
            }
        }
    }

}
