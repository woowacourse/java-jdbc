package org.springframework.jdbc.core;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionExecutor<T> {

    T execute(Connection connection);
}
