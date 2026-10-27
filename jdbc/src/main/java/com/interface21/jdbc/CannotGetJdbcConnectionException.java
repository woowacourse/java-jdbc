package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;

public class CannotGetJdbcConnectionException extends DataAccessException {

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
