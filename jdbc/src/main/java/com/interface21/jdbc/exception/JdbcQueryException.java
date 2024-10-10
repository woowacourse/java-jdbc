package com.interface21.jdbc.exception;

import java.sql.SQLException;

public class JdbcQueryException extends RuntimeException {

    public JdbcQueryException(String msg) {
        super(msg);
    }

    public JdbcQueryException(String msg, SQLException ex) {
        super(msg, ex);
    }

    public JdbcQueryException(String msg, IllegalStateException ex) {
        super(msg, ex);
    }
}

