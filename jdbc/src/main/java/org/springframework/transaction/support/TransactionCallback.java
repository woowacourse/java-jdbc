package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionCallback {

    void execute();
}
