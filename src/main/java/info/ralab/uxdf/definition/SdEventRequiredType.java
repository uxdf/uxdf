package info.ralab.uxdf.definition;

/**
 * SD Event对于Node是否必须类型
 */
public enum SdEventRequiredType {
    /**
     * 对于左侧Node必须
     */
    left,
    /**
     * 对于右侧Node必须
     */
    right,
    /**
     * 对于两侧Node必须
     */
    both,
    /**
     * 不必须
     */
    none
}
