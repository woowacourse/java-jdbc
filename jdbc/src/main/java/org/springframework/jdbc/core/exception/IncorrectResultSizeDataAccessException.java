package org.springframework.jdbc.core.exception;

import org.springframework.dao.DataAccessException;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(final String message) {
        super(message);
    }
}
