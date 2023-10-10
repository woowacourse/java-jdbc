package org.springframework.jdbc;

import java.sql.SQLException;

public class CannotGetJdbcConnectionException extends RuntimeException {

    public CannotGetJdbcConnectionException(final String msg) {
        super(msg);
    }

    public CannotGetJdbcConnectionException(final String msg, final SQLException ex) {
        super(msg, ex);
    }

    public CannotGetJdbcConnectionException(final String msg, final IllegalStateException ex) {
        super(msg, ex);
    }
}
