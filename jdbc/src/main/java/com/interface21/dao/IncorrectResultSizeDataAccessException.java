package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private final int expectedSize;

    public IncorrectResultSizeDataAccessException(final String message, final int expectedSize) {
        super(message);
        this.expectedSize = expectedSize;
    }

    public int getExpectedSize() {
        return expectedSize;
    }
}
