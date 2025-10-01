package com.interface21.jdbc.core.exception;

public class DataAccessException extends RuntimeException {

    public DataAccessException(final String message) {
        super(message);
    }

    public DataAccessException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
