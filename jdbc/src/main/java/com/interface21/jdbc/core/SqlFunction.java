package com.interface21.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
interface SqlFunction<T, R> {

    R apply(final T t) throws SQLException;
}
