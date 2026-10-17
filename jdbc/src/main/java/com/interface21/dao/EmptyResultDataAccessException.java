package com.interface21.dao;

public class EmptyResultDataAccessException extends DataAccessException {
    public EmptyResultDataAccessException() {
        super("결과가 없습니다.");
    }
}
