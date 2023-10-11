package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionCallbackWithoutResult {

    void doBizLogic();

}
