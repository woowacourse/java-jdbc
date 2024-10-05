package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super(String.format("결과 크기가 불일치합니다. expectedSize: %d, actualSize: %d", expectedSize, actualSize));
    }
}
