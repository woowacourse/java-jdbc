package org.springframework.transaction;

import java.sql.Connection;

public interface TransactionTemplate {

    void execute(Connection connection);
}
