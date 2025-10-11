package com.interface21.exception;

import java.sql.SQLException;

public class ResultBindingException extends DataAccessException {

    public ResultBindingException(String msg) {
        super(msg);
    }

    public ResultBindingException(String msg, SQLException ex) {
        super(msg, ex);
    }

    public ResultBindingException(String msg, IllegalStateException ex) {
        super(msg, ex);
    }
}
