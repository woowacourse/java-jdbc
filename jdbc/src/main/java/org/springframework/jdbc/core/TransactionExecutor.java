package org.springframework.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionExecutor<T> {

    T execute() throws SQLException;
}
