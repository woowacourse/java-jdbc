package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> extractResults(ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }
}
