package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;

public class NonUniqueResultException extends DataAccessException {
    public NonUniqueResultException(final String message) {
        super(message);
    }
}
