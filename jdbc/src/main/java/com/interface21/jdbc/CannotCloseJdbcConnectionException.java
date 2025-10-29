package com.interface21.jdbc;

import java.sql.SQLException;

public class CannotCloseJdbcConnectionException extends RuntimeException {

    public CannotCloseJdbcConnectionException(String msg) {
        super(msg);
    }

    public CannotCloseJdbcConnectionException(String msg, SQLException ex) {
        super(msg, ex);
    }

    public CannotCloseJdbcConnectionException(String msg, IllegalStateException ex) {
        super(msg, ex);
    }
}
