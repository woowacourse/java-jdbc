package org.springframework.jdbc.core.exception;

public class ConnectionContextException extends RuntimeException {

    public ConnectionContextException(final Throwable cause) {
        super(cause);
    }
}
