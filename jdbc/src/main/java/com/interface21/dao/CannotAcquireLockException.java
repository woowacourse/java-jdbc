package com.interface21.dao;

public class CannotAcquireLockException extends DataAccessException {

    public CannotAcquireLockException(Throwable cause) {
        super(cause);
    }
}
