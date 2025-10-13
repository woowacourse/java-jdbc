package com.interface21.jdbc.exception;

public class DataAccessException extends RuntimeException {

    public DataAccessException(final String message, final Throwable t) {
        super(message, t);
    }
}
