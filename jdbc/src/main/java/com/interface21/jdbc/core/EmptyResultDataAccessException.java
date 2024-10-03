package com.interface21.jdbc.core;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    public EmptyResultDataAccessException(int actualSize) {
        super(0, actualSize);
    }
}
