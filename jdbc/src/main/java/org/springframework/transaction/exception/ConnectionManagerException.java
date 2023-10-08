package org.springframework.transaction.exception;

import java.sql.SQLException;

public class ConnectionManagerException extends RuntimeException {

    public ConnectionManagerException(final SQLException e) {
        super(e);
    }
}
