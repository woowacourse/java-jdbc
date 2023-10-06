package org.springframework.transaction;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionTask {

    void execute(Connection connection);
}
