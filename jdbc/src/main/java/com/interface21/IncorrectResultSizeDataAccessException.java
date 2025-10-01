package com.interface21;

import com.interface21.jdbc.CustomizedDataAccessException;

public class IncorrectResultSizeDataAccessException extends CustomizedDataAccessException {
    public IncorrectResultSizeDataAccessException(String sql, int expected, int actual) {
        super("Expected " + expected + " row(s), but got " + actual + " for SQL: " + sql, null);
    }
}

