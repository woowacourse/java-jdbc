package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionCallback<T> {

    T doInConnection(Connection connection, PreparedStatement preparedStatement) throws SQLException;

}
