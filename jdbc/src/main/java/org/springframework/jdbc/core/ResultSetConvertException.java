package org.springframework.jdbc.core;

public class ResultSetConvertException extends RuntimeException {

    public ResultSetConvertException(String message, Throwable e) {
        super(message, e);
    }

    public ResultSetConvertException(String message) {
        super(message);
    }
}
