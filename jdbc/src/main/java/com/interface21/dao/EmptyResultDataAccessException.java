package com.interface21.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    public EmptyResultDataAccessException(final int expectedSize) {
        super("조회 결과가 없습니다. 기대: %d, 실제: 0".formatted(expectedSize));
    }
}
