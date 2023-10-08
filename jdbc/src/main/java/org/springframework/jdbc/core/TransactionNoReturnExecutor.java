package org.springframework.jdbc.core;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionNoReturnExecutor {

    void execute(Connection connection);
}
