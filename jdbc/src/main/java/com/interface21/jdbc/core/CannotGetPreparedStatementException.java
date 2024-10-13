package com.interface21.jdbc.core;

import java.sql.SQLException;

public class CannotGetPreparedStatementException extends RuntimeException {

    public CannotGetPreparedStatementException(String message, SQLException exception) {
        super(message, exception);
    }
}
