package org.springframework.transaction.support;

import java.sql.Connection;

public interface TransactionExecutor<T> {

    T execute(Connection connection);
}
