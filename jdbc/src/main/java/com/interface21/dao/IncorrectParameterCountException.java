package com.interface21.dao;

public class IncorrectParameterCountException extends DataAccessException {

    public IncorrectParameterCountException(int expectedCount, int actualCount) {
        super(String.format("파라미터 개수가 일치하지 않습니다. expectedCount: %d, actualCount: %d", expectedCount, actualCount));
    }
}
