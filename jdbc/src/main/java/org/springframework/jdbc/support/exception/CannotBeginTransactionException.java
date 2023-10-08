package org.springframework.jdbc.support.exception;

import org.springframework.dao.DataAccessException;

public class CannotBeginTransactionException extends DataAccessException {

    public CannotBeginTransactionException(final String message) {
        super(message);
    }
}
