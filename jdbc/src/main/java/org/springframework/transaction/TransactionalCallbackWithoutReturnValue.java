package org.springframework.transaction;

@FunctionalInterface
public interface TransactionalCallbackWithoutReturnValue {

    void execute();
}
