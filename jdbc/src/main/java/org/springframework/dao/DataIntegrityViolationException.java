package org.springframework.dao;

import java.sql.SQLException;

public class DataIntegrityViolationException extends DataAccessException {

    private static final String EXCEPTION_MESSAGE = "DataIntegrityViolationException was occured with sql : ";

    public DataIntegrityViolationException(String msg) {
        super(EXCEPTION_MESSAGE + msg);
    }

    public DataIntegrityViolationException(String msg, SQLException ex) {
        super(EXCEPTION_MESSAGE + msg, ex);
    }
}
