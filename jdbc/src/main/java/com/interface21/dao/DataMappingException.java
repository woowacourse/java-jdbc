package com.interface21.dao;

public class DataMappingException extends DataAccessException {
    public DataMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataMappingException(String message) {
        super(message);
    }
}
