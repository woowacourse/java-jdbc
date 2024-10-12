package com.interface21.jdbc;

import java.sql.SQLException;

public class CannotGetJdbcConnectionException extends RuntimeException {

    public CannotGetJdbcConnectionException(String message) {
        super(message);
    }

    public CannotGetJdbcConnectionException(String message, SQLException exception) {
        super(message, exception);
    }

    public CannotGetJdbcConnectionException(String message, IllegalStateException exception) {
        super(message, exception);
    }
}
