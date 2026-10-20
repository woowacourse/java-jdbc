package com.interface21.dao;

public class DuplicateKeyException extends DataAccessException {

    public DuplicateKeyException(final Throwable cause) {
        super(cause);
    }
}
