package org.springframework.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    public EmptyResultDataAccessException() {
        super("검색되는 데이터가 없습니다.");
    }
}
