package org.springframework.transaction;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionTemplate<T> {

    T execute(final Connection connection);
}
