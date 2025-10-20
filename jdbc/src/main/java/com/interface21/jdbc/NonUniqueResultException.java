package com.interface21.jdbc;

public class NonUniqueResultException extends RuntimeException {
    public NonUniqueResultException(final String message) {
        super(message);
    }
}
