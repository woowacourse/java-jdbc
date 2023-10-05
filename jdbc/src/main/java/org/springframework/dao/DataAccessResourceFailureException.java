package org.springframework.dao;

import java.sql.SQLException;

public class DataAccessResourceFailureException extends DataAccessException {

    private static final String EXCEPTION_MESSAGE = "DataAccessResourceFailureException was occured with sql : ";

    public DataAccessResourceFailureException(String msg) {
        super(EXCEPTION_MESSAGE + msg);
    }

    public DataAccessResourceFailureException(String msg, SQLException ex) {
        super(EXCEPTION_MESSAGE + msg, ex);
    }
}
