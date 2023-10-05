package org.springframework.jdbc.core.exception;

import org.springframework.dao.DataAccessException;

public class ConnectionContextException extends DataAccessException {

    public ConnectionContextException(final Throwable cause) {
        super(cause);
    }
}
