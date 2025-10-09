package com.interface21.jdbc.core;

import java.sql.SQLException;

public class JdbcFailException extends RuntimeException {

    public JdbcFailException(final SQLException exception) {
        super(exception);
    }
}
