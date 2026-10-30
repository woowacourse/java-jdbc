package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;

import java.sql.SQLException;

public class CannotGetJdbcConnectionException extends DataAccessException {

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
