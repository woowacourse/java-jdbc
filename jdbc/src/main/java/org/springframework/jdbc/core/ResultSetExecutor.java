package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetExecutor<T> {
    T extractData(ResultSet resultSet) throws SQLException;
}
