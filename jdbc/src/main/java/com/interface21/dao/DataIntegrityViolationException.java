package com.interface21.dao;

public class DataIntegrityViolationException extends DataAccessException {

    public DataIntegrityViolationException(Throwable cause) {
        super(cause);
    }
}
