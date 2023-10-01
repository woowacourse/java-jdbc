package org.springframework.transaction.support;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionCallback {

    void execute(Connection connection);
}
