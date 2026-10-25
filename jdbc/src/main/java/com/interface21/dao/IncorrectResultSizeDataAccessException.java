package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        super("조회 결과 개수가 올바르지 않습니다. expected: " + expectedSize + ", actual: " + actualSize);
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
