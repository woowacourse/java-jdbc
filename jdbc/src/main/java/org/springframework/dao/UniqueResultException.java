package org.springframework.dao;

public class UniqueResultException extends RuntimeException {

    public UniqueResultException(String message) {
        super(message);
    }
}
