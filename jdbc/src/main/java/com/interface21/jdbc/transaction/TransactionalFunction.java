package com.interface21.jdbc.transaction;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionalFunction {

    void execute(Connection connection) throws SQLException;
}
