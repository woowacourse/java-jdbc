package com.interface21.jdbc.exception;

public class DatabaseAccessException extends RuntimeException {
    public DatabaseAccessException(final String message) {
        super(message);
    }

    public DatabaseAccessException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
