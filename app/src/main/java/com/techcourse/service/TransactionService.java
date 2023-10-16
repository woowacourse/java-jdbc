package com.techcourse.service;

public abstract class TransactionService<T> {

    protected T appService;

    protected TransactionService(final T appService) {
        this.appService = appService;
    }
}
