package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        this("조회 결과 개수가 올바르지 않습니다. 기대: %d, 실제: %d".formatted(expectedSize, actualSize));
    }

    protected IncorrectResultSizeDataAccessException(final String message) {
        super(message);
    }
}
