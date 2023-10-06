package org.springframework.exception;

import org.springframework.dao.DataAccessException;

public class EmptyResultException extends DataAccessException {

    public EmptyResultException(final String message) {
        super(message);
    }
}
