package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface ResultSetExecutor<T> {

    List<T> execute(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException;
}
