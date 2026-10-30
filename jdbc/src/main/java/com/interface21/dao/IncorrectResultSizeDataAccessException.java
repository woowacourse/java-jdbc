package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public IncorrectResultSizeDataAccessException(final String message) {
        super(message);
    }
}
