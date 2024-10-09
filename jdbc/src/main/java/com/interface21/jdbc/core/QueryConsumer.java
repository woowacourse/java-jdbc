package com.interface21.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface QueryConsumer<T> {

    void accept(T t) throws SQLException;
}
