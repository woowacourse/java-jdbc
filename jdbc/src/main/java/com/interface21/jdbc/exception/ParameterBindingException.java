package com.interface21.jdbc.exception;

public class ParameterBindingException extends JdbcException {
    public ParameterBindingException(Throwable cause, String sql) {
        super("Failed to bind parameters", cause, sql);
    }
}
