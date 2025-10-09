package com.interface21.jdbc.exception;

public class RowMappingException extends JdbcException {
    public RowMappingException(Throwable cause, String sql) {
        super("Failed to row mapping", cause, sql);
    }
}
