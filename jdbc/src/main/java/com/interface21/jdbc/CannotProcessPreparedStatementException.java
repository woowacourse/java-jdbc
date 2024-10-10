package com.interface21.jdbc;

import java.sql.SQLException;

public class CannotProcessPreparedStatementException extends RuntimeException {

    public CannotProcessPreparedStatementException(String msg, SQLException ex) {
        super(msg, ex);
    }
}
