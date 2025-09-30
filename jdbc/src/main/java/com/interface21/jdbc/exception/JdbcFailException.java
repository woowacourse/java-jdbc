package com.interface21.jdbc.exception;

public class JdbcFailException extends RuntimeException {

    private String sqlState;
    private String message;

    public JdbcFailException(String sqlState, String message) {
        this.sqlState = sqlState;
        this.message = message;
    }

    public JdbcFailException(String message) {
        this.message=message;
    }
}
