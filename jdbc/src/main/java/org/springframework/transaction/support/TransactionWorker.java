package org.springframework.transaction.support;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionWorker {

    void run(final Connection connection);
}
