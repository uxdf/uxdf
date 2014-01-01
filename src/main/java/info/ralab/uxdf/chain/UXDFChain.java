package info.ralab.uxdf.chain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import info.ralab.uxdf.UXDFException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UXDF关系链
 */
public class UXDFChain implements Iterable<List<UXDFChainItem>> {

    /**
     * 关系链从左向右指向路径
     */
    public final static String PATH_LEFT = ">";
    /**
     * 关系链从右向左指向路径
     */
    public final static String PATH_RIGHT = "<";
    /**
     * 关系链路径
     */
    public final static String PATH_LINE = "-";
    /**
     * 关系链匹配正则
     */
    private final static Pattern PATH_PATTERN = Pattern.compile("[" + PATH_RIGHT + PATH_LEFT + PATH_LINE + "]");

    /**
     * 解析后的关系链项目集合
     */
    private List<List<UXDFChainItem>> items;

    /**
     * 标签和Sd映射
     */
    private Map<String, String> labelSdMapping = Maps.newHashMap();

    /**
     * 获取标签和Sd名称的映射关系{@link Map}，其中标签作为键。
     *
     * @return 标签和Sd映射关系的Map
     */
    public Map<String, String> getLabelSdMapping() {
        return Collections.unmodifiableMap(this.labelSdMapping);
    }

    /**
     * 基于关系链字符串生成关系链{@link UXDFChain}
     *
     * @param chainString 关系链字符串
     */
    private UXDFChain(final String chainString) {
        // 初始化关系链项目集合。先加入一个空的项目集合
        items = Lists.newArrayList();
        items.add(Lists.newArrayList(new UXDFChainItem()));

        // 从当前关系链字符串中查找匹配的项
        Matcher matcher = PATH_PATTERN.matcher(chainString);

        // 截取起始位置，通过匹配位置获取
        int begin = 0;
        while (matcher.find()) {
            // 截取串当中的Sd名称和路径方向
            int start = matcher.start();
            String sd = chainString.substring(begin, start);
            begin = start + 1;
            String path = chainString.substring(start, begin);

            // 如果一个item设置完成，则新建一个item，并且把当前sd放入新建的item中
            UXDFChainItem lastItem = null;
            boolean itemFinished = true;
            // 遍历关系链集合，并像最后一个关系链中，添加截取出的Sd和路径
            for (List<UXDFChainItem> itemList : items) {
                UXDFChainItem item = itemList.get(itemList.size() - 1);
                // 获取是否最后一个关系链已经完整
                itemFinished = item.addSd(sd);
                // 记录最后一次设置的关系链
                lastItem = item;
            }

            // 是否结束
            if (itemFinished) {
                // 检查是否通配
                if (lastItem != null && lastItem.hasWildcards()) { // 包含通配
                    Set<UXDFChainItem> itemSet = lastItem.createItemsByWildcards();

                    // 通配集合为空，清除最后一个Item，并且终止chain的解析。
                    if (itemSet.isEmpty()) {
                        for (List<UXDFChainItem> itemList : items) {
                            itemList.remove(itemList.size() - 1);
                        }
                        break;
                    }
                    // 扩展item集合
                    List<List<UXDFChainItem>> wildcardsItems = Lists.newArrayList();
                    itemSet.forEach(wildcardsItem -> {
                        // 遍历已有关系链集合，将通配的集合加入其中
                        this.items.forEach(itemList -> {
                            // 替换末尾的Chain Item
                            itemList.set(itemList.size() - 1, wildcardsItem);

                            // 创建新的chain集合
                            List<UXDFChainItem> copyItemList = Lists.newArrayList(itemList);
                            wildcardsItems.add(copyItemList);

                            // 末尾添加下一个解析Item
                            UXDFChainItem nextItem = new UXDFChainItem();
                            nextItem.setFirstNode(wildcardsItem.getLastNode());
                            copyItemList.add(nextItem);
                        });
                    });
                    // 替换items
                    this.items = wildcardsItems;
                } else {
                    // 末尾添加下一个解析Item
                    for (List<UXDFChainItem> itemList : items) {
                        UXDFChainItem nextItem = new UXDFChainItem();
                        nextItem.addSd(sd);
                        itemList.add(nextItem);
                    }
                }
            }

            // 设置路径方向
            for (List<UXDFChainItem> itemList : items) {
                UXDFChainItem item = itemList.get(itemList.size() - 1);
                item.addPath(path);
            }
        }
        // 最后一个Sd名称
        if (begin < chainString.length()) {
            // 截取SD名称
            String sd = chainString.substring(begin);
            UXDFChainItem lastItem = null;
            for (List<UXDFChainItem> itemList : items) {
                // 获取结尾的关系串
                UXDFChainItem item = itemList.get(itemList.size() - 1);
                boolean itemFinished = item.addSd(sd);
                // 最后一个名称如果不是结束，则认为路径错误
                if (!itemFinished) {
                    throw new UXDFException(String.format("uxdf chain [%s] incomplete.", chainString));
                }
                lastItem = item;
            }
            // 检查是否通配
            if (lastItem != null && lastItem.hasWildcards()) { // 包含通配
                Set<UXDFChainItem> itemSet = lastItem.createItemsByWildcards();
                // 通配集合为空，清除最后一个Item，并且终止chain的解析
                if (itemSet.isEmpty()) {
                    for (List<UXDFChainItem> itemList : items) {
                        itemList.remove(itemList.size() - 1);
                    }
                } else {
                    // 扩展item集合
                    List<List<UXDFChainItem>> wildcardsItems = Lists.newArrayList();
                    itemSet.forEach(wildcardsItem -> {
                        // 遍历已有关系链集合
                        this.items.forEach(itemList -> {
                            // 替换末尾的关系链
                            itemList.set(itemList.size() - 1, wildcardsItem);

                            // 创建新的chain集合
                            List<UXDFChainItem> copyItemList = Lists.newArrayList(itemList);
                            wildcardsItems.add(copyItemList);
                        });
                    });
                    // 替换items
                    this.items = wildcardsItems;
                }
            }
        }

        // 遍历所有chainItem设置链表属性 设置标签和Sd名称的映射关系
        this.items.forEach(chainItemList -> {
            for (int i = 0; i < chainItemList.size(); i++) {
                UXDFChainItem chainItem = chainItemList.get(i);
                // 设置当前ChainItem位于关系链中位置
                chainItem.setItemList(i, chainItemList);
                // 获取当前ChainItem的标签和Sd名称映射关系
                List<Map.Entry<String, String>> labelSd = Lists.newArrayList(
                        new AbstractMap.SimpleEntry<>(chainItem.getFirstLabel(), chainItem.getFirstNode()),
                        new AbstractMap.SimpleEntry<>(chainItem.getLastLabel(), chainItem.getLastNode()),
                        new AbstractMap.SimpleEntry<>(chainItem.getEventLabel(), chainItem.getEvent())
                );
                labelSd.forEach((labelSdMap) -> {
                    String label = labelSdMap.getKey();
                    String sd = labelSdMap.getValue();
                    // 检查映射关系是否已经存在
                    labelSdMapping.compute(label, (key, value) -> {
                        // 对应当前标签的value不存在 或 和当前标签及Sd名称完全一致 认为正确
                        if (value == null || sd.equals(value)) {
                            return sd;
                        } else {
                            throw new UXDFException(String.format(
                                    "标签[%s]在关系链[%s]中重复出现。",
                                    label,
                                    chainString
                            ));
                        }
                    });
                });
            }
        });
    }


    /**
     * 通过一个表达式，创建UXDF链实例
     *
     * @param chainString 关系链字符串
     * @return 关系链
     */
    public static UXDFChain getInstance(final String chainString) throws UXDFException {
        if (chainString == null || chainString.isEmpty()) {
            return null;
        }
        return new UXDFChain(chainString);
    }

    /**
     * 判断字符串是否有关系表达式
     *
     * @param string 字符串
     * @return 是否有关系表达式
     */
    public static boolean haveRelationship(String string) {
        return PATH_PATTERN.matcher(string).find();
    }

    @Override
    public Iterator<List<UXDFChainItem>> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        this.items.forEach(uxdfChainItems -> {
            builder.append("[");
            uxdfChainItems.forEach(chainItem -> {
                builder.append("(");
                builder.append(chainItem.toString());
                builder.append("),");
            });
            builder.append("],");
        });
        builder.append("]");
        return builder.toString();
    }
}
