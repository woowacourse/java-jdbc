package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.function.Supplier;

public interface TransactionManager {

    void beginTransaction(Runnable task);

    <T> T beginTransaction(Supplier<T> task);

    void rollback(Connection conn);
}
