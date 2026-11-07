package com.interface21.transaction;

import com.interface21.jdbc.core.JdbcOperations;

@FunctionalInterface
public interface TransactionWork {
    void execute(JdbcOperations jdbc);
}
