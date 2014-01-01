package info.ralab.uxdf.chain;

import com.google.common.collect.Sets;
import info.ralab.uxdf.UXDFException;
import info.ralab.uxdf.UXDFLoader;
import info.ralab.uxdf.definition.SdEventDefinition;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static info.ralab.uxdf.chain.UXDFChain.PATH_LEFT;
import static info.ralab.uxdf.chain.UXDFChain.PATH_LINE;
import static info.ralab.uxdf.chain.UXDFChain.PATH_RIGHT;


/**
 * UXDF语法链解析结果项
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "valueOf")
public class UXDFChainItem {

    public static final String SD_WILDCARDS = "*";
    public static final String DELIMITER_LABEL = ":";
    public static final Pattern PATTERN_LABEL = Pattern.compile(DELIMITER_LABEL);
    private static final AtomicLong LABEL_SEQ = new AtomicLong();

    private static String tempTabel() {
        return "L_" + LABEL_SEQ.getAndIncrement();
    }

    @NonNull
    private String firstNode;

    private String firstLabel;

    @NonNull
    private String leftPath;

    @NonNull
    private String event;

    private String eventLabel;

    @NonNull
    private String rightPath;

    @NonNull
    private String lastNode;

    private String lastLabel;

    private ChainPath chainPath;

    @Getter
    private int itemIndex;

    @Getter
    private List<UXDFChainItem> chainItems;

    /**
     * 添加SD，必须按照Node、Event、Node的顺序添加。
     *
     * @param sd 添加的SD名称
     * @return 返回当前ChainItem添加SD是否完成
     */
    public boolean addSd(final String sd) {
        if (sd == null || sd.isEmpty()) {
            throw new UXDFException("uxdf chain sd is not null or empty.");
        }
        // 解析别名标签
        String[] labelAndSd = PATTERN_LABEL.split(sd);
        if (labelAndSd.length > 2) {
            throw new UXDFException(String.format("Sd只能设置一个别名。[%s]", sd));
        }

        if (this.firstNode == null) {

            if (labelAndSd.length == 2) { // 同时设置了别名和Sd
                this.firstLabel = labelAndSd[0];
                this.firstNode = labelAndSd[1];
            } else { // 只设置了Sd
                this.firstLabel = labelAndSd[0];
                this.firstNode = labelAndSd[0];
            }
            if (this.firstNode.equals(SD_WILDCARDS)) {
                throw new UXDFException("Start Sd can not use \"*\".");
            }
            return false;
        } else if (this.event == null) {
            if (labelAndSd.length == 2) { // 同时设置了别名和Sd
                this.eventLabel = labelAndSd[0];
                this.event = labelAndSd[1];
            } else { // 只设置了Sd
                this.eventLabel = labelAndSd[0];
                this.event = labelAndSd[0];
            }
            if (this.eventLabel.equals(SD_WILDCARDS)) { // 通配符没有别名，生成临时别名
                this.eventLabel = tempTabel();
            }
            return false;
        } else if (this.lastNode == null) {
            if (labelAndSd.length == 2) { // 同时设置了别名和Sd
                this.lastLabel = labelAndSd[0];
                this.lastNode = labelAndSd[1];
            } else { // 只设置了Sd
                this.lastLabel = labelAndSd[0];
                this.lastNode = labelAndSd[0];
            }
            if (this.lastLabel.equals(SD_WILDCARDS)) { // 通配符没有别名，生成临时别名
                this.lastLabel = tempTabel();
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * 添加路径，只能添加两个路径
     *
     * @param path 路径字符串
     */
    public void addPath(final String path) {
        // 无效路径
        if (!PATH_LEFT.equals(path) && !PATH_RIGHT.equals(path) && !PATH_LINE.equals(path)) {
            throw new UXDFException(String.format(
                    "uxdf chain path only use : '%s, %s, %s'",
                    PATH_LEFT,
                    PATH_RIGHT,
                    PATH_LINE
            ));
        }

        if (this.leftPath == null) {
            // 校验路径是否合法，左路径不能是一个PATH_LEFT(从左向右的方向)。
            if (PATH_LEFT.equals(path)) {
                throw new UXDFException(String.format("uxdf chain path left only use : %s or %s", PATH_LINE, PATH_RIGHT));
            }

            // 左路径为空，设置左路径
            this.leftPath = path;
        } else if (this.rightPath == null) {
            // 校验路径是否合法，右路径不能是一个PATH_RIGHT(从右向左的方向)。
            if (PATH_RIGHT.equals(path)) {
                throw new UXDFException(String.format("uxdf chain path left only use : %s or %s", PATH_LINE, PATH_LEFT));
            }

            // 右路径为空，设置右路径
            this.rightPath = path;

            // 校验路径是否合法，右路径在左路径是PATH_RIGHT(从右向左的方向)时，不能是一个PATH_LEFT(从左向右的方向)。
            if (this.leftPath.equals(PATH_RIGHT) && this.rightPath.equals(PATH_LEFT)) {
                throw new UXDFException(String.format(
                        "uxdf chain item [%s%s%s%s%s] format error.",
                        this.firstNode,
                        this.leftPath,
                        this.event,
                        this.rightPath,
                        this.rightPath
                ));
            }

            // 确定Item的方向
            if (this.leftPath.equals(PATH_RIGHT)) {
                this.chainPath = ChainPath.RIGHT;
            } else if (this.rightPath.equals(PATH_LEFT)) {
                this.chainPath = ChainPath.LEFT;
            } else {
                this.chainPath = ChainPath.BOTH;
            }

        }
    }

    /**
     * 获取当前关系串中的左Node
     *
     * @return 左Node名称
     */
    public String getLeftNodeName() {
        switch (chainPath) {
            case LEFT:
                return firstNode;
            case RIGHT:
                return lastNode;
            default:
                return null;
        }
    }

    /**
     * 获取左Node的标签
     *
     * @return 左Node的标签
     */
    public String getLeftNodeLabel() {
        switch (chainPath) {
            case LEFT:
                return firstLabel;
            case RIGHT:
                return lastLabel;
            default:
                return null;
        }
    }

    /**
     * 获取当前关系串中的右Node
     *
     * @return 右Node名称
     */
    public String getRightNodeName() {
        switch (chainPath) {
            case RIGHT:
                return firstNode;
            case LEFT:
                return lastNode;
            default:
                return null;
        }
    }

    /**
     * 获取右Node的标签
     *
     * @return 右Node的标签
     */
    public String getRightNodeLabel() {
        switch (chainPath) {
            case RIGHT:
                return firstLabel;
            case LEFT:
                return lastLabel;
            default:
                return null;
        }
    }

    /**
     * 是否包含通配符
     *
     * @return 是否包含
     */
    public boolean hasWildcards() {
        return this.event.equals(SD_WILDCARDS)
                || this.lastNode.equals(SD_WILDCARDS)
                || this.chainPath.equals(ChainPath.BOTH);
    }

    /**
     * 基于通配符创建{@link UXDFChainItem}集合
     *
     * @return 关系链集合
     */
    public Set<UXDFChainItem> createItemsByWildcards() {
        // Node未配置
        if (UXDFLoader.getNode(this.firstNode) == null) {
            throw new UXDFException(String.format("node [%s] not defined.", this.firstNode));
        }
        // 通配之后的所有关系链集合
        Set<UXDFChainItem> items = Sets.newHashSet();
        // 由第一个Node获取所有相关的Event
        Set<SdEventDefinition> events = UXDFLoader.getEventsByNodeName(this.firstNode);

        // Node没有任何关联Event，返回空的关系链集合
        if (events == null || events.isEmpty()) {
            return items;
        }

        // 基于当前关系链的方向进行通配
        switch (this.chainPath) {
            case LEFT:
                // 从左向右
                fillWildcardsItemWithLeft(items, events);
                break;
            case RIGHT:
                // 从右向左
                fillWildcardsItemWithRight(items, events);
                break;
            case BOTH:
                // 双向
                fillWildcardsItemWithLeft(items, events);
                fillWildcardsItemWithRight(items, events);
                break;
        }

        return items;
    }

    /**
     * 从左向右填充
     *
     * @param items  通配填充后的关系链集合
     * @param events 需要进行填充Event集合
     */
    private void fillWildcardsItemWithLeft(Set<UXDFChainItem> items, Set<SdEventDefinition> events) {
        if (this.event.equals(SD_WILDCARDS) && this.lastNode.equals(SD_WILDCARDS)) {
            // Event和RightNode都是通配符
            // 遍历所有Event定义
            events.forEach(sdEventDefinition -> {
                // 如果LeftNode和当前不匹配则跳过
                if (!sdEventDefinition.getLeftNodeName().equals(this.getFirstNode())) {
                    return;
                }
                // 构建新的ChainItem
                items.add(this.cloneBySdWithLeft(sdEventDefinition));
            });
        } else if (this.event.equals(SD_WILDCARDS)) {
            // Event是通配符
            // 遍历所有Event定义
            events.forEach(sdEventImpl -> {
                // 如果LeftNode，RightNode和当前不匹配则跳过
                if (!sdEventImpl.getLeftNodeName().equals(this.getFirstNode())
                        || !sdEventImpl.getRightNodeName().equals(this.getLastNode())) {
                    return;
                }
                // 构建新的ChainItem
                items.add(this.cloneBySdWithLeft(sdEventImpl));
            });
        } else if (this.lastNode.equals(SD_WILDCARDS)) {
            // RightNode是通配符
            // 遍历所有Event定义
            events.forEach(sdEventImpl -> {
                // 如果LeftNode，Event和当前不匹配则跳过
                if (!sdEventImpl.getLeftNodeName().equals(this.getFirstNode())
                        || !sdEventImpl.getEventName().equals(this.getEvent())) {
                    return;
                }
                // 构建新的ChainItem
                items.add(this.cloneBySdWithLeft(sdEventImpl));
            });
        } else {
            // 没有通配符，只是方向是双向
            // 遍历所有Event定义
            events.forEach(sdEventImpl -> {
                // 如果LeftNode，Event，RightNode和当前不匹配则跳过
                if (!sdEventImpl.getLeftNodeName().equals(this.getFirstNode())
                        || !sdEventImpl.getEventName().equals(this.getEvent())
                        || !sdEventImpl.getRightNodeName().equals(this.getLastNode())) {
                    return;
                }
                // 构建新的ChainItem
                items.add(this.cloneBySdWithLeft(sdEventImpl));
            });
        }
    }

    /**
     * 从右向左填充
     *
     * @param items  通配填充后的关系链集合
     * @param events 需要进行填充Event集合
     */
    private void fillWildcardsItemWithRight(Set<UXDFChainItem> items, Set<SdEventDefinition> events) {
        if (this.event.equals(SD_WILDCARDS) && this.lastNode.equals(SD_WILDCARDS)) {
            // Event和RightNode都是通配符
            // 遍历所有Event定义
            events.forEach(sdEventImpl -> {
                // 如果RightNode和当前不匹配则跳过
                if (!sdEventImpl.getRightNodeName().equals(this.getFirstNode())) {
                    return;
                }
                // 构建新的ChainItem
                items.add(this.cloneBySdWithRight(sdEventImpl));
            });
        } else if (this.event.equals(SD_WILDCARDS)) {
            // Event是通配符
            // 遍历所有Event定义
            events.forEach(sdEventImpl -> {
                // 如果LeftNode，RightNode和当前不匹配则跳过
                if (!sdEventImpl.getLeftNodeName().equals(this.getLastNode()) ||
                        !sdEventImpl.getRightNodeName().equals(this.getFirstNode())) {
                    return;
                }
                // 构建新的ChainItem
                items.add(this.cloneBySdWithRight(sdEventImpl));
            });
        } else if (this.lastNode.equals(SD_WILDCARDS)) {
            // RightNode是通配符
            // 遍历所有Event定义
            events.forEach(sdEventImpl -> {
                // 如果RightNode，Event和当前不匹配则跳过
                if (!sdEventImpl.getRightNodeName().equals(this.getFirstNode()) ||
                        !sdEventImpl.getEventName().equals(this.getEvent())) {
                    return;
                }
                // 构建新的ChainItem
                items.add(this.cloneBySdWithRight(sdEventImpl));
            });
        } else {
            // 没有通配符，只是方向是双向
            // 遍历所有Event定义
            events.forEach(sdEventImpl -> {
                // 如果LeftNode，Event，RightNode和当前不匹配则跳过
                if (!sdEventImpl.getRightNodeName().equals(this.getFirstNode())
                        || !sdEventImpl.getEventName().equals(this.getEvent())
                        || !sdEventImpl.getLeftNodeName().equals(this.getLastNode())) {
                    return;
                }
                // 构建新的ChainItem
                items.add(this.cloneBySdWithRight(sdEventImpl));
            });
        }
    }

    /**
     * 基于当前关系链复制一个从左向右的关系链
     *
     * @param sdEventDefinition 复制关系链的Event
     * @return 复制的关系链
     */
    private UXDFChainItem cloneBySdWithLeft(final SdEventDefinition sdEventDefinition) {
        UXDFChainItem targetItem = new UXDFChainItem();
        // 复制标签
        copyLabel(targetItem);
        // 目标的起始Node为当前的左Node
        targetItem.firstNode = sdEventDefinition.getLeftNodeName();

        targetItem.leftPath = PATH_LINE;

        targetItem.event = sdEventDefinition.getEventName();

        targetItem.rightPath = PATH_LEFT;
        // 目标的结束Node为当前右Node
        targetItem.lastNode = sdEventDefinition.getRightNodeName();
        // 目标关系链的方向为左向右
        targetItem.chainPath = ChainPath.LEFT;
        return targetItem;
    }

    /**
     * 基于当前关系链复制一个从右向左的关系链
     *
     * @param sdEventDefinition 复制关系链的Event
     * @return 复制的关系链
     */
    private UXDFChainItem cloneBySdWithRight(final SdEventDefinition sdEventDefinition) {
        UXDFChainItem item = new UXDFChainItem();
        // 复制标签
        copyLabel(item);
        // 目标的起始Node为当前的右Node
        item.firstNode = sdEventDefinition.getRightNodeName();

        item.leftPath = PATH_RIGHT;

        item.event = sdEventDefinition.getEventName();

        item.rightPath = PATH_LINE;
        // 目标的结束Node为当前的左Node
        item.lastNode = sdEventDefinition.getLeftNodeName();
        // 目标关系链的方向为右向左
        item.chainPath = ChainPath.RIGHT;
        return item;
    }

    /**
     * 将当前关系链标签复制给目标关系链
     *
     * @param targetItem 目标关系链
     */
    private void copyLabel(final UXDFChainItem targetItem) {
        targetItem.firstLabel = this.firstLabel;

        targetItem.eventLabel = this.eventLabel;

        targetItem.lastLabel = this.lastLabel;
    }

    public void setItemList(final int index, final List<UXDFChainItem> chainItemList) {
        this.itemIndex = index;
        this.chainItems = chainItemList;
    }

    /**
     * 当前{@link UXDFChainItem}之后是否还有{@link UXDFChainItem}
     *
     * @return 之后是否还有关系链
     */
    public boolean hasNext() {
        return this.chainItems.size() - 1 > this.itemIndex;
    }

    /**
     * 获取下一个{@link UXDFChainItem}
     *
     * @return 当前关系链的后一个关系链
     */
    public UXDFChainItem next() {
        return hasNext() ? this.chainItems.get(this.itemIndex + 1) : null;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
                obj.getClass().equals(this.getClass()) &&
                obj.hashCode() == this.hashCode();
    }

    @Override
    public String toString() {
        String content = "";
        content += (this.firstLabel == null ? "" : this.firstLabel + DELIMITER_LABEL);
        content += (this.firstNode);
        content += (this.leftPath);
        content += (this.eventLabel == null ? "" : this.eventLabel + DELIMITER_LABEL);
        content += (this.event);
        content += (this.rightPath);
        content += (this.lastLabel == null ? "" : this.lastLabel + DELIMITER_LABEL);
        content += (this.lastNode);
        return content;
    }
}

