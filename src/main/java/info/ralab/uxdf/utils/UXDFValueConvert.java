package info.ralab.uxdf.utils;

/**
 * UXDF数据转换接口
 *
 * @param <T>
 */
public interface UXDFValueConvert<T> {
    T convert(Object value);
}
