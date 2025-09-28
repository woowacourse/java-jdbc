package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super(String.format("Incorrect result size: expected %d, actual %d", expectedSize, actualSize));
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
