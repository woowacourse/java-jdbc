package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;

public class TransactionalException extends DataAccessException {

    public TransactionalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
