package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionExecutor {

    void apply(Connection connection) throws SQLException;
}
