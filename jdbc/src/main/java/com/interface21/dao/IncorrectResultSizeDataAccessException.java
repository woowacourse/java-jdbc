package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(final int actualSize) {
        super("Expected at most one row but found " + actualSize);
    }
}
