package com.interface21.jdbc.core.utils;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class SQLExceptionHandler {

    private static Logger log = LoggerFactory.getLogger(SQLExceptionHandler.class);

    private SQLExceptionHandler() {
    }

    public static <T> T handleSQLException(SQLException e) {
        log.error(e.getMessage(), e);
        throw new DataAccessException(e);
    }
}
