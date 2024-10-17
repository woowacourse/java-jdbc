package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionConsumer {

    void accept(Connection connection) throws SQLException;
}
