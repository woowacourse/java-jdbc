package org.springframework.dao;

public final class IncorrectResultSizeDataAccessException extends RuntimeException{

    public IncorrectResultSizeDataAccessException() {
        super("유일하지 않은 데이터 row입니다.");
    }
}
