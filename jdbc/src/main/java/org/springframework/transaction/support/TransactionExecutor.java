package org.springframework.transaction.support;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionExecutor<T> {

    T execute(Connection connection);
}
