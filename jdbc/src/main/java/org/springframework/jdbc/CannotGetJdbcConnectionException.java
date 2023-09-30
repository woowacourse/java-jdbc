package org.springframework.jdbc;

import java.sql.SQLException;

public class CannotGetJdbcConnectionException extends JdbcException {

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
