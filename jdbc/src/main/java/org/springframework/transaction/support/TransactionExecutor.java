package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionExecutor {

    void execute(Connection connection) throws SQLException;
}
