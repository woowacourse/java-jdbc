package com.interface21.transaction.support;

@FunctionalInterface
public interface TransactionalWork {
    void execute() throws Exception;
}
