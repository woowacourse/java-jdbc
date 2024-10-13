package com.interface21.jdbc.core;

public class ConnectionCloseException extends RuntimeException {

    public ConnectionCloseException(String message) {
        super(message);
    }

    public ConnectionCloseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
