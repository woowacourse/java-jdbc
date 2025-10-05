package com.interface21.jdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecution<T, R> {

    R apply(final T t) throws SQLException;
}
