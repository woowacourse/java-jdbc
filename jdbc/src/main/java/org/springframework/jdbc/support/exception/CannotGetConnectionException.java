package org.springframework.jdbc.support.exception;

import org.springframework.dao.DataAccessException;

public class CannotGetConnectionException extends DataAccessException {

    public CannotGetConnectionException(final String message) {
        super(message);
    }
}
