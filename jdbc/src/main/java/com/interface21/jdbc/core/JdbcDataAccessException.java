package com.interface21.jdbc.core;

import java.sql.SQLException;

public class JdbcDataAccessException extends RuntimeException {

    public JdbcDataAccessException(String message) {
        super(message);
    }

    public JdbcDataAccessException(String message, SQLException exception) {
        super(message, exception);
    }
}
