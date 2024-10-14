package com.interface21.jdbc.exception;

public class SqlExecutionException extends RuntimeException {

    public SqlExecutionException(String message) {
        super(message);
    }

    public SqlExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
