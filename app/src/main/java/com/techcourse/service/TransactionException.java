package com.techcourse.service;

public class TransactionException extends RuntimeException {

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
