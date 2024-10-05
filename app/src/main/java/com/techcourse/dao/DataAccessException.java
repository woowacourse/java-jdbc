package com.techcourse.dao;

public class DataAccessException extends RuntimeException {

    public DataAccessException(Throwable e) {
        super(e);
    }
}
