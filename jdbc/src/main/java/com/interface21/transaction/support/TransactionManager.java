package com.interface21.transaction.support;

import java.sql.Connection;

public interface TransactionManager {

    void beginTransaction(Runnable task);

    void rollback(Connection conn);
}
