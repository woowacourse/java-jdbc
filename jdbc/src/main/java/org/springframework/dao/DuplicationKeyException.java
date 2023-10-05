package org.springframework.dao;

import java.sql.SQLException;

public class DuplicationKeyException extends DataAccessException {

    private static final String EXCEPTION_MESSAGE = "DuplicationKeyException was occured with sql : ";

    public DuplicationKeyException(String msg) {
        super(EXCEPTION_MESSAGE + msg);
    }

    public DuplicationKeyException(String msg, SQLException ex) {
        super(EXCEPTION_MESSAGE + msg, ex);
    }
}
