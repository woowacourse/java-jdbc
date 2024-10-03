package com.interface21.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {
    public EmptyResultDataAccessException() {
        super("결과가 비어있습니다.");
    }
}
