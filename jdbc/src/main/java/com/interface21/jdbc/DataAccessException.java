package com.interface21.jdbc;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(final SQLException cause) {
        super(cause);
    }
}
