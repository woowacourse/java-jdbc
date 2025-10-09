package com.interface21.jdbc.exception;

public abstract class JdbcException extends RuntimeException {

    private final String sql;

    public JdbcException(String message, Throwable cause, String sql) {
        super(message, cause);
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
