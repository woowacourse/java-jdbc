package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapResultSetExecutor<T> implements ResultSetExecutor<List<T>> {

    private final RowMapper<T> rowMapper;

    public RowMapResultSetExecutor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> extractData(final ResultSet resultSet) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }
}
