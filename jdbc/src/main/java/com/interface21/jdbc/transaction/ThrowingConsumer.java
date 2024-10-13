package com.interface21.jdbc.transaction;

import java.sql.SQLException;

@FunctionalInterface
interface ThrowingConsumer<T> {

    void accept(T connection) throws SQLException;
}
