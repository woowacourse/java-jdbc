package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetExecutor<T> {
    T execute(ResultSet resultSet) throws SQLException;
}
