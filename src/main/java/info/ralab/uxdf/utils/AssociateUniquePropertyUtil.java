package info.ralab.uxdf.utils;

import info.ralab.uxdf.UXDFException;
import info.ralab.uxdf.UXDFLoader;
import info.ralab.uxdf.chain.ChainPath;
import info.ralab.uxdf.chain.UXDFChain;
import info.ralab.uxdf.chain.UXDFChainItem;
import info.ralab.uxdf.definition.SdBaseType;
import info.ralab.uxdf.definition.SdDefinition;
import info.ralab.uxdf.definition.SdNodeDefinition;
import info.ralab.uxdf.definition.SdProperty;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 关联唯一属性辅助类
 */
public class AssociateUniquePropertyUtil {
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("^unique(\\w+)Id$");

    /**
     * 判断属性名称是否是符合关联唯一属性命名规范
     *
     * @param propertyName 属性名称
     * @return 是否符合
     */
    public static boolean isAssociateUniquePropertyName(final String propertyName) {
        SdNodeDefinition nodeDefinition = getNodeDefinitionByPropertyName(propertyName);
        return nodeDefinition != null;
    }

    /**
     * 构造关联唯一属性的SdProperty
     *
     * @return 关联唯一属性定义
     */
    public static SdProperty getProperty(final String propertyName) {
        if (!isAssociateUniquePropertyName(propertyName)) {
            return null;
        }
        SdProperty sdProperty = new SdProperty();
        sdProperty.setBase(SdBaseType.Integer);
        sdProperty.setRequired(true);
        return sdProperty;
    }

    /**
     * 根据Sd定义得到关联唯一属性名称
     *
     * @param sdDefinition Sd定义
     * @return 关联唯一的属性名称
     */
    public static String getPropertyName(final SdDefinition sdDefinition) {
        if (sdDefinition == null) {
            return null;
        }
        // 获取唯一属性
        String[] uniqueIndexs = sdDefinition.getUniqueIndex();
        if (uniqueIndexs == null) {
            return null;
        }

        String redundancyPropertyName = null;

        // 记录语法链数量
        int uxdfChainCount = 0;

        for (String uniqueIndex : uniqueIndexs) {
            if (!UXDFChain.haveRelationship(uniqueIndex)) {
                continue;
            }

            // 获取语法链
            UXDFChain uxdfChain = UXDFChain.getInstance(uniqueIndex);
            List<UXDFChainItem> items = uxdfChain.iterator().next();
            if (items.isEmpty()) {
                throw new UXDFException(String.format("语法链 [%s] 错误", uniqueIndex));
            }
            uxdfChainCount++;
            if (uxdfChainCount > 1) {
                throw new UXDFException(String.format("SD [%s] 定义的唯一属性包含多个语法链", sdDefinition.getTitle()));
            }

            UXDFChainItem uxdfChainItem = items.get(0);
            // 拼接冗余属性名称
            String redundancyNodeName;
            if (ChainPath.RIGHT.equals(uxdfChainItem.getChainPath())) {
                redundancyNodeName = uxdfChainItem.getLeftNodeName();
            } else if (ChainPath.LEFT.equals(uxdfChainItem.getChainPath())) {
                redundancyNodeName = uxdfChainItem.getRightNodeName();
            } else {
                throw new UXDFException(String.format("不支持该类型语法链 [%s]", uniqueIndex));
            }
            redundancyPropertyName = String.format("unique%sId", redundancyNodeName);
        }

        return redundancyPropertyName;
    }

    /**
     * 根据关联唯一属性名称得到标题
     *
     * @param propertyName 关联唯一属性名
     * @return 关联唯一属性标题
     */
    public static String getPropertyTitle(String propertyName) {
        SdNodeDefinition nodeDefinition = getNodeDefinitionByPropertyName(propertyName);
        if (nodeDefinition == null) {
            return null;
        }
        return nodeDefinition.getTitle() + "关联标识";
    }

    /**
     * 根据关联唯一属性名称获取关联的SdNode定义{@link SdNodeDefinition}
     *
     * @param propertyName 关联唯一属性名称
     * @return SdNode定义
     */
    private static SdNodeDefinition getNodeDefinitionByPropertyName(final String propertyName) {

        if (propertyName == null) {
            return null;
        }
        Matcher matcher = PROPERTY_PATTERN.matcher(propertyName);
        if (!matcher.find()) {
            return null;
        }

        return UXDFLoader.getNode(matcher.group(1));
    }
}
