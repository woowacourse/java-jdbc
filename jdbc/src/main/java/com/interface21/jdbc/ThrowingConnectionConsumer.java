package com.interface21.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ThrowingConnectionConsumer {

    void accept(Connection connection) throws SQLException;
}
