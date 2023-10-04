package org.springframework.jdbc.core.exception;

import org.springframework.dao.DataAccessException;

public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException(final String message) {
        super(message);
    }
}
