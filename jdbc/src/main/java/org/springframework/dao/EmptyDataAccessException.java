package org.springframework.dao;

public class EmptyDataAccessException extends DataAccessException {

    public EmptyDataAccessException() {
        super("조회 결과가 없습니다.");
    }
}
