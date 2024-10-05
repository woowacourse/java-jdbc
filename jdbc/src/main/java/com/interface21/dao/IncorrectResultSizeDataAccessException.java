package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private final int expectedSize;

    private final int actualSize;

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super("Incorrect result size. expected size: %d, actual size: %d".formatted(expectedSize, actualSize));
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
