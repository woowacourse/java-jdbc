package com.interface21.dao;

public class DuplicateKeyException extends DataAccessException {

    public DuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
