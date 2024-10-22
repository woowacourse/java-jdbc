package com.interface21.transaction;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionCallback {

    void doInConnection(Connection connection) throws SQLException;
}
