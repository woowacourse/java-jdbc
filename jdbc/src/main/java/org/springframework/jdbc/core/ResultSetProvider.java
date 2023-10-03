package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetProvider<T> {

    private final RowMapper<T> rowMapper;

    public ResultSetProvider(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public <T> T getResults(final ResultSet resultSet) {
        try {
            if (resultSet.getFetchSize() > 1) {
                return (T) getList(resultSet);
            }
            return (T) getOne(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public T getOne(final ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            final T result = rowMapper.mapRow(resultSet);
            return result;
        }
        return null;
    }

    public List<T> getList(final ResultSet resultSet) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (!resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

}
