package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionExecutor {

    void execute(Connection connection) throws SQLException;

}
