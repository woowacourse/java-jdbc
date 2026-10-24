package com.interface21.dao;

public class DuplicateKeyException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public DuplicateKeyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
