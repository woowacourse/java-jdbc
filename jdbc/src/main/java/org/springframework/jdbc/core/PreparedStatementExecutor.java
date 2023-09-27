package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface PreparedStatementExecutor<T> {
    T fetchData(ResultSet resultSet) throws SQLException;

    ResultSet fetchResultSet(PreparedStatement preparedStatement) throws SQLException;
}
