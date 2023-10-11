package org.springframework.transaction;

import java.sql.SQLException;

public class TransactionSystemException extends RuntimeException {

    public TransactionSystemException(String msg) {
        super(msg);
    }

    public TransactionSystemException(String msg, SQLException ex) {
        super(msg, ex);
    }
}
