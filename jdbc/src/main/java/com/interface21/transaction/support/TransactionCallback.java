package com.interface21.transaction.support;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback {
    void execute(Transaction transaction) throws SQLException;
}
