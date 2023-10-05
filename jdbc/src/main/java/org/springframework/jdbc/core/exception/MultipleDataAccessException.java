package org.springframework.jdbc.core.exception;

import org.springframework.dao.DataAccessException;

public class MultipleDataAccessException extends DataAccessException {

    public MultipleDataAccessException(final String message) {
        super(message);
    }
}
