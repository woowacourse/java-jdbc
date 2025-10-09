package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

public class InvalidResultException extends DataAccessException {

    public InvalidResultException(String message) {
        super(message);
    }
}
