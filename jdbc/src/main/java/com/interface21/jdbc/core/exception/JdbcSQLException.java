package com.interface21.jdbc.core.exception;

public class JdbcSQLException extends RuntimeException {

    public JdbcSQLException(Throwable cause) {
        super(cause);
    }

    public JdbcSQLException(String message, Throwable cause) {
        super(message, cause);
    }
}
