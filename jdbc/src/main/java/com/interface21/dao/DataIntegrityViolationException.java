package com.interface21.dao;

public class DataIntegrityViolationException extends DataAccessException {

    public DataIntegrityViolationException() {
        super("Key constraint fails");

    }
}
