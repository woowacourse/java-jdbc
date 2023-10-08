package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionExecuteStrategy {

    void strategy(final Connection connection) throws SQLException;
}
