package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;

public class CustomizedDataAccessException extends DataAccessException {

    public CustomizedDataAccessException(String sql, Throwable cause) {
        super("error executing SQL: " + sql, cause);
    }
}
