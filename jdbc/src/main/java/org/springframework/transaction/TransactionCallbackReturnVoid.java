package org.springframework.transaction;

@FunctionalInterface
public interface TransactionCallbackReturnVoid {

    void execute();
}
