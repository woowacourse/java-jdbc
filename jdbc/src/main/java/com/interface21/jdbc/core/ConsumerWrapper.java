package com.interface21.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface ConsumerWrapper<T> {
    void accept(T t) throws SQLException;
}
