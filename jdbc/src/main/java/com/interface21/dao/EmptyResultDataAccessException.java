package com.interface21.dao;

public class EmptyResultDataAccessException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public EmptyResultDataAccessException(String message) {
        super(message);
    }

    public EmptyResultDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
