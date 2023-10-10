package org.springframework.transaction;

public interface TransactionalOperationWithReturn<T> {

    T execute();
}
