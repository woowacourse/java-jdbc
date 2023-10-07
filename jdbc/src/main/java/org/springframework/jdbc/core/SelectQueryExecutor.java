package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SelectQueryExecutor<T> {

    T execute(final ResultSet resultSet) throws SQLException;
}
