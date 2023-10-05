package org.springframework.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException{

    public EmptyResultDataAccessException() {
        super(0);
    }
}
