package com.techcourse.service;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionalDaoExecutor<T> {

    T execute(final Connection connection);
}
