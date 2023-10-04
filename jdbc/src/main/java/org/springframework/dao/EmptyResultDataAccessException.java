package org.springframework.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    public EmptyResultDataAccessException(int expectedSize) {
        super(expectedSize, 0);
    }
}
