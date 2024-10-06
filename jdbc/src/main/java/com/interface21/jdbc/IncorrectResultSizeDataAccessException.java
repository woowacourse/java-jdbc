package com.interface21.jdbc;

import java.sql.SQLException;

public class IncorrectResultSizeDataAccessException extends RuntimeException {
    public IncorrectResultSizeDataAccessException(String msg) {
        super(msg);
    }

    public IncorrectResultSizeDataAccessException(String msg, SQLException ex) {
        super(msg, ex);
    }

    public IncorrectResultSizeDataAccessException(String msg, IllegalStateException ex) {
        super(msg, ex);
    }
}
