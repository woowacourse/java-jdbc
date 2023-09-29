package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

    private final RowMapper<T> rowMapper;

    public RowMapperResultSetExtractor(final RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> extractData(final ResultSet resultSet) throws SQLException {
        final List<T> results = new ArrayList<>();
        int rowNumber = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNumber));
        }

        return results;
    }
}
