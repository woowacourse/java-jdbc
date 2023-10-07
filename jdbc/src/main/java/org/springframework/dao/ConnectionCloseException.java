package org.springframework.dao;

public class ConnectionCloseException extends RuntimeException {

    public ConnectionCloseException() {
        super();
    }

    public ConnectionCloseException(final String message) {
        super(message);
    }

    public ConnectionCloseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConnectionCloseException(final Throwable cause) {
        super(cause);
    }

    public ConnectionCloseException(final String message, final Throwable cause, final boolean enableSuppression,
                                    final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
