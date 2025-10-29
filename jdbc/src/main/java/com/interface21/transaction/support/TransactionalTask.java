package com.interface21.transaction.support;

@FunctionalInterface
public interface TransactionalTask {

    void execute() throws Exception;
}
