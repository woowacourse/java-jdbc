package com.interface21.jdbc;

public class JdbcExecutionException extends RuntimeException {

    public JdbcExecutionException(String message) {
        super(message);
    }

    public JdbcExecutionException(String message, Exception e) {
        super(message, e);
    }
}
