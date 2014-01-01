package info.ralab.uxdf.executor;

/**
 * 执行器，用于完成不定长参数请求的处理。
 */
public interface Executor<T> {

    /**
     * 执行请求，获取结果。
     *
     * @return 执行结果。
     */
    T execute();

}
