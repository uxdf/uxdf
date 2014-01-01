package info.ralab.uxdf.model;

/**
 * 查询逻辑条件
 */
public enum SdDataQueryLogic {
    /**
     * 等于
     */
    EQ,
    /**
     * 不等于
     */
    NE,
    /**
     * 大于
     */
    GT,
    /**
     * 小于
     */
    LT,
    /**
     * 大于等于
     */
    GTE,
    /**
     * 小于等于
     */
    LTE,
    /**
     * 相似
     */
    LIKE,
    /**
     * 起始相等
     */
    SW,
    /**
     * 结尾相等
     */
    EW,
    /**
     * 为NULL
     */
    NULL,
    /**
     * 不为NULL
     */
    NN,
    /**
     * 存在
     */
    EXIST,
    /**
     * 不存在
     */
    NEX;

    /**
     * 判断字符串是否是逻辑条件
     *
     * @param name 字符串
     * @return 是否是逻辑条件
     */
    public static boolean contains(final String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        for (SdDataQueryLogic queryLogic : values()) {
            if (queryLogic.name().equals(name)) {
                return true;
            }
        }

        return false;
    }

}
