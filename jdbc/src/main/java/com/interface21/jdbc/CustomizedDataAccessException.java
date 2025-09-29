package com.interface21.jdbc;

public class CustomizedDataAccessException extends RuntimeException {

    public CustomizedDataAccessException(String sql, Throwable cause) {
        super("error executing SQL: " + sql, cause);
    }
}
