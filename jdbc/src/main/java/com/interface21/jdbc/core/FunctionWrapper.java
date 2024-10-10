package com.interface21.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface FunctionWrapper<T, U> {
    U apply(T t) throws SQLException;
}
