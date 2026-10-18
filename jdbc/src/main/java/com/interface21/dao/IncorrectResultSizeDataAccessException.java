package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        super("결과값이 2개 이상입니다. 예상한 값: " + expectedSize + ", 실제 값: " + actualSize);
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
