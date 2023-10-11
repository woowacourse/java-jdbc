package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nullable;

@FunctionalInterface
public interface TransactionExecuter<T> {
    @Nullable
    T execute(Connection connection) throws SQLException;
}
