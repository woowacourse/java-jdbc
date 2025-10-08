package com.interface21.jdbc.core;

public class DataAccessApiException extends RuntimeException {

    public DataAccessApiException(Throwable cause) {
        super(cause);
    }

    public DataAccessApiException(String message) {
        super(message);
    }
}
