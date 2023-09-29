package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetObjectMapper<T> {

    T map(final ResultSet resultSet) throws SQLException;
}
