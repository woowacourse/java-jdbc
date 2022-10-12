package com.techcourse.service;

@FunctionalInterface
public interface TransactionalExecutor {
    void execute();
}
