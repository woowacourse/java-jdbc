package com.interface21.transaction;

import com.interface21.dao.DataAccessException;

public class TransactionException extends DataAccessException {

    public TransactionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
