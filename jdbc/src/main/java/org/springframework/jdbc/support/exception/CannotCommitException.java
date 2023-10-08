package org.springframework.jdbc.support.exception;

import org.springframework.dao.DataAccessException;

public class CannotCommitException extends DataAccessException {

    public CannotCommitException(final String message) {
        super(message);
    }
}
