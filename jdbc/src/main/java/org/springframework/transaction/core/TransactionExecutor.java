package org.springframework.transaction.core;

import java.sql.Connection;

public interface TransactionExecutor<T> {

    T execute(Connection connection);
}
