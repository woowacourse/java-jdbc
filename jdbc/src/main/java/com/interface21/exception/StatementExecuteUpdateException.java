package com.interface21.exception;

import java.sql.SQLException;

public class StatementExecuteUpdateException extends DataAccessException {

    public StatementExecuteUpdateException(String msg) {
        super(msg);
    }

    public StatementExecuteUpdateException(String msg, SQLException ex) {
        super(msg, ex);
    }

    public StatementExecuteUpdateException(String msg, IllegalStateException ex) {
        super(msg, ex);
    }
}
