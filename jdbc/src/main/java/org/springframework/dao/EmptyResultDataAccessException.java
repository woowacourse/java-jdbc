package org.springframework.dao;

public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException(final String message) {
        super(message);
    }
}
