package com.techcourse.service.transaction;

@FunctionalInterface
public interface TransactionCommandExecutor {

    void run();
}
