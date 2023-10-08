package org.springframework.jdbc.support.exception;

import org.springframework.dao.DataAccessException;

public class CannotRollbackException extends DataAccessException {

    public CannotRollbackException(final String message) {
        super("Rollback Failed: " + message);
    }
}
