package com.interface21.jdbc.exception;

public class ConnectionCloseException extends RuntimeException {

    public ConnectionCloseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
