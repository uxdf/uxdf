package info.ralab.uxdf;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * UXDF异常
 */
public class UXDFException extends RuntimeException {
    public UXDFException() {
        super();
    }

    public UXDFException(final Exception e) {
        super(e);
    }

    public UXDFException(final String message) {
        super(message);
    }

    public UXDFException(final String message, final Exception e) {
        super(message, e);
    }


    @Override
    @JSONField(serialize = false)
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

}
