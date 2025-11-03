package com.interface21.transaction;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallbackWithoutResult {
    void doInTransaction() throws SQLException;
}

