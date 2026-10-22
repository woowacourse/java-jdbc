package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        this("조회 결과 개수가 다릅니다. expected=" + expectedSize + ", actual=" + actualSize, expectedSize, actualSize);
    }

    protected IncorrectResultSizeDataAccessException(final String message, final int expectedSize, final int actualSize) {
        super(message);
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
