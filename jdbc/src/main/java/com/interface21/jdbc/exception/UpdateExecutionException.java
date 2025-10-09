package com.interface21.jdbc.exception;

public class UpdateExecutionException extends JdbcException {
    public UpdateExecutionException(Throwable cause, String sql) {
        super("Failed to execute update", cause, sql);
    }
}
