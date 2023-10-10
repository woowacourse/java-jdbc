package org.springframework.transaction;

public interface TransactionServiceExecutor<T> {

    T execute();

}
