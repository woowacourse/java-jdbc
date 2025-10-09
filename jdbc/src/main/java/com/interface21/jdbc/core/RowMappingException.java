package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

public class RowMappingException extends DataAccessException {

    public RowMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}