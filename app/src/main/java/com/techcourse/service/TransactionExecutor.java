package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionExecutor<T> {
    T execute(final Connection connection) throws SQLException;
}
