package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super("result 크기가 잘못되었습니다: expected " + expectedSize + ", actual " + actualSize);
    }
}
