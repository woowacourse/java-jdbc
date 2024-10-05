package com.interface21.jdbc;

public class DataAccessException extends RuntimeException {

    public DataAccessException(String msg) {
        super(msg);
    }

    public DataAccessException(Exception ex) {
        super(ex);
    }
}
