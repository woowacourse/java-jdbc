package org.springframework.transaction.support;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionExecuteStrategy<T> {

    T strategy() throws SQLException;
}
