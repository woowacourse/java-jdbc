package com.interface21.dao;

public class SqlExecutionException extends DataAccessException {

    public SqlExecutionException(final String message) {
        super(message);
    }

    public SqlExecutionException(final Throwable cause) {
        super(cause);
    }
}
