package org.springframework.jdbc;

import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

public class CannotCloseJdbcConnectionException extends DataAccessException {

    public CannotCloseJdbcConnectionException(String msg) {
        super(msg);
    }

    public CannotCloseJdbcConnectionException(String msg, SQLException ex) {
        super(msg, ex);
    }
}
