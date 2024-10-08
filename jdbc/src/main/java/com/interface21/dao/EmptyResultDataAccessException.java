package com.interface21.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    public EmptyResultDataAccessException() {
        super(1, 0);
    }
}
