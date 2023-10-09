package org.springframework.transaction.support;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionCallback {

    void doInTransaction(Connection connection);
}
