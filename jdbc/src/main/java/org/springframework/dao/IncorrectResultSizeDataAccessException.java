package org.springframework.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException() {
        super("잘못된 응답 데이터 크기입니다.");
    }

    public IncorrectResultSizeDataAccessException(final String message) {
        super(message);
    }
}
