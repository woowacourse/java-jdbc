package com.interface21.jdbc.core;

public class SqlExecutionException extends RuntimeException {

    public SqlExecutionException(String message) {
        super(message);
    }

    public SqlExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
