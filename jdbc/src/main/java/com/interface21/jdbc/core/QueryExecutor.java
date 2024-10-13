package com.interface21.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface QueryExecutor<T, R> {

    R execute(T t) throws SQLException;
}
