package org.springframework.jdbc.exception;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }
}
