package com.interface21.jdbc.core;

import java.sql.SQLException;

public class JdbcFailException extends RuntimeException {

    private String message;

    public JdbcFailException(final SQLException exception) {
        super(exception);
    }

    public JdbcFailException(String message) {
        this.message = message;
    }
}
