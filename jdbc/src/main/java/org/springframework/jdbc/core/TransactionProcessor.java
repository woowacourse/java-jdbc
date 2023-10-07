package org.springframework.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionProcessor {
    void process() throws SQLException;
}
