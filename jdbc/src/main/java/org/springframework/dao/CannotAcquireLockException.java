package org.springframework.dao;

import java.sql.SQLException;

public class CannotAcquireLockException extends DataAccessException {

    private static final String EXCEPTION_MESSAGE = "CannotAcquireLockException was occured with sql : ";

    public CannotAcquireLockException(String msg) {
        super(EXCEPTION_MESSAGE + msg);
    }

    public CannotAcquireLockException(String msg, SQLException ex) {
        super(EXCEPTION_MESSAGE + msg, ex);
    }
}
