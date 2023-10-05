package org.springframework.transaction;

import java.sql.Connection;

public interface TransactionTask {

    void execute(Connection connection);
}
