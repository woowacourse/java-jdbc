package com.interface21.dao;

public class DuplicateKeyException extends DataAccessException {
    public DuplicateKeyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
