package com.interface21.dao;

public class EmptyResultDataAccessException extends RuntimeException {

    public EmptyResultDataAccessException() {
        super();
    }

    public EmptyResultDataAccessException(String message) {
        super(message);
    }
}
