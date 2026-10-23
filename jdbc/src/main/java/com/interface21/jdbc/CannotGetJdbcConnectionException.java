package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;

public class CannotGetJdbcConnectionException extends DataAccessException {

    public CannotGetJdbcConnectionException(String message) {
        super(message);
    }

    public CannotGetJdbcConnectionException(String message, SQLException cause) {
        super(message, cause);
    }
}
