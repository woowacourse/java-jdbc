package com.interface21.jdbc;

public class NonUniqueResultException extends RuntimeException {

    public NonUniqueResultException(String msg) {
        super(msg);
    }
}
