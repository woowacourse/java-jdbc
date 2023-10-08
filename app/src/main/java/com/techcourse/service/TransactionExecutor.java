package com.techcourse.service;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionExecutor<T> {

    T execute(Connection connection);
}
