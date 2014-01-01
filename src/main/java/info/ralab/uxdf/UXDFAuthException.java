package info.ralab.uxdf;

import lombok.Getter;

public class UXDFAuthException extends UXDFException {
    /**
     * 已认证
     */
    @Getter
    private boolean authed;

    public UXDFAuthException(final String message) {
        super(message);
    }

    public UXDFAuthException(final String message, final Exception e) {
        super(message, e);
    }

    public UXDFAuthException(final String message, final boolean authed) {
        super(message);
        this.authed = authed;
    }

    public UXDFAuthException(final String message, final boolean authed, Exception e) {
        super(message, e);
        this.authed = authed;
    }
}
