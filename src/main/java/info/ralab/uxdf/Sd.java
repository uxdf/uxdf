package info.ralab.uxdf;

import info.ralab.uxdf.definition.SdEvent;
import info.ralab.uxdf.definition.SdNode;
import lombok.Data;

/**
 * SD结构定义
 */
@Data
public class Sd {

    /**
     * Node定义部分
     */
    private SdNode node = new SdNode();
    /**
     * Event定义部分
     */
    private SdEvent event = new SdEvent();

    /**
     * 将另外的Sd定义{@link Sd}合并至当前Sd定义
     *
     * @param sd 另外的Sd定义
     */
    public void merge(final Sd sd) {
        if (sd == null) {
            return;
        }
        this.node.merge(sd.getNode());
        this.event.merge(sd.getEvent());
    }

}
