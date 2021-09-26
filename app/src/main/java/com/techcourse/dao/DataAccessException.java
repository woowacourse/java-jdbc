package com.techcourse.dao;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(SQLException exception) {
        super(exception.getMessage(), exception.getCause());
    }
}
