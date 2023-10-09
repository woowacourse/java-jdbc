package org.springframework.jdbc.core;

public interface TransactionalOperationWithReturn<T> {

    T execute() throws Exception;
}
