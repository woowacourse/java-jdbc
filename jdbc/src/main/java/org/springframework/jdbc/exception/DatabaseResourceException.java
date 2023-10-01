package org.springframework.jdbc.exception;

import java.sql.SQLException;

public class DatabaseResourceException extends RuntimeException {

    public DatabaseResourceException(String message, SQLException e) {
        super(message, e);
    }
}
