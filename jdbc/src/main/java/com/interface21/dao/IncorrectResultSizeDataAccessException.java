package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        super("Expected " + expectedSize + " row(s), but found " + actualSize);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    public int getExpectedSize() {
        return expectedSize;
    }

    public int getActualSize() {
        return actualSize;
    }
}
