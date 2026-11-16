package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;

import java.sql.SQLException;

public class JdbcConnectionCloseException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public JdbcConnectionCloseException(final SQLException cause) {
        super("Failed to close JDBC Connection", cause);
    }
}
