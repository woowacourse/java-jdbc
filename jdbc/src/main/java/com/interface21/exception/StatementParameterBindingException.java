package com.interface21.exception;

import java.sql.SQLException;

public class StatementParameterBindingException extends DataAccessException {

    public StatementParameterBindingException(String msg) {
        super(msg);
    }

    public StatementParameterBindingException(String msg, SQLException ex) {
        super(msg, ex);
    }

    public StatementParameterBindingException(String msg, IllegalStateException ex) {
        super(msg, ex);
    }
}

