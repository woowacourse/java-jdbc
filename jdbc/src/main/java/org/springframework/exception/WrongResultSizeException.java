package org.springframework.exception;

import org.springframework.dao.DataAccessException;

public class WrongResultSizeException extends DataAccessException {

    public WrongResultSizeException(final String message) {
        super(message);
    }
}
