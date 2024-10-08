package com.interface21.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface QueryFunction<T, R> {

    R apply(T t) throws SQLException;
}
