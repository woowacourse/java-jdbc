package org.springframework.dao;

public class ResultSetConvertException extends DataAccessException {

    public ResultSetConvertException(String message, Throwable e) {
        super(message, e);
    }

    public ResultSetConvertException(String message) {
        super(message);
    }
}
