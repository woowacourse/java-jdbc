package org.springframework.transaction.support;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback<T> {

    T doInTransaction() throws SQLException;
}
