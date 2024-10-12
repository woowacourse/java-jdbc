package com.interface21.jdbc.exception;

import java.sql.SQLException;


public class SQLQueryException extends RuntimeException {

    public SQLQueryException(SQLException e) {
        super(e);
    }
}
