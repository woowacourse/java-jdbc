package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionProcessor {
    void process(final Connection connection) throws SQLException;
}
