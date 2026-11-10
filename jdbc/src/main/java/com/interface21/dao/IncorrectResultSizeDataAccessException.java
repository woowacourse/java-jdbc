package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        super("조회 결과 건수가 올바르지 않습니다. 기대: " + expectedSize + "건, 실제: " + actualSize + "건");
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
