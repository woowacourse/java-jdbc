package org.springframework.transaction;

import java.sql.SQLException;

public class TransactionRollbackException extends RuntimeException {

    public TransactionRollbackException(String msg) {
        super(msg);
    }

    public TransactionRollbackException(String msg, SQLException ex) {
        super(msg, ex);
    }
}
