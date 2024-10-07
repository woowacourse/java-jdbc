package com.interface21.jdbc.exception;

public class ConnectionFailException extends RuntimeException {
    public ConnectionFailException(final String message, final Throwable cause) {
        super(message,cause);
    }
}
