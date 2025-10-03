package com.interface21.jdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetMapper<T, R> {

    R map(T t) throws SQLException;
}
