package com.interface21.exception;

import java.sql.SQLException;

public class StatementExecuteQueryException extends DataAccessException {

    public StatementExecuteQueryException(String msg) {
        super(msg);
    }

    public StatementExecuteQueryException(String msg, SQLException ex) {
        super(msg, ex);
    }

    public StatementExecuteQueryException(String msg, IllegalStateException ex) {
        super(msg, ex);
    }
}

