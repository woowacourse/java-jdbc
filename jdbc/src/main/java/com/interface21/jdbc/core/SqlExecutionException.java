package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

public class SqlExecutionException extends DataAccessException {

    public SqlExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
