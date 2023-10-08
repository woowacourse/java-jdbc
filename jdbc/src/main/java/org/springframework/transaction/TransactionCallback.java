package org.springframework.transaction;

@FunctionalInterface
public interface TransactionCallback {

    void execute();
}
