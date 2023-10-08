package org.springframework.transaction;

import java.sql.Connection;

public interface TransactionExecutor {

    void execute(Connection connection);
}
