package com.techcourse.service;

public class TransactionFailedException extends RuntimeException {

    public TransactionFailedException(Throwable e) {
        super(e);
    }
}
