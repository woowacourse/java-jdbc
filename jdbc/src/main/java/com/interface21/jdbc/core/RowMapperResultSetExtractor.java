package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<T> {

    private final RowMapper<T> mapper;

    public RowMapperResultSetExtractor(final RowMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<T> extractData(final ResultSet resultSet) throws SQLException {
        final List<T> result = new ArrayList<>();

        while (resultSet.next()) {
            result.add(mapper.mapRow(resultSet));
        }

        return result;
    }
}
