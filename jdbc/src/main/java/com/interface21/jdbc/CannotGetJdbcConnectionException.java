package com.interface21.jdbc;

import java.sql.SQLException;

public class CannotGetJdbcConnectionException extends RuntimeException {

    public CannotGetJdbcConnectionException(String msg) {
        super(msg);
    }

    public CannotGetJdbcConnectionException(String msg, SQLException ex) {
        super(msg, ex);
    }

    public CannotGetJdbcConnectionException(String msg, IllegalStateException ex) {
        super(msg, ex);
    }
}
