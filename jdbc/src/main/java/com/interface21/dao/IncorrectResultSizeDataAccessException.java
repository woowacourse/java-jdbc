package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(int size) {
        super("결과의 개수(%s)가 잘못되었습니다.".formatted(size));
    }

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }
}
