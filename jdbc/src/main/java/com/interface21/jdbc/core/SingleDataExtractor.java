package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SingleDataExtractor implements DataExtractor {

    @Override
    public <T> List<T> extract(final ResultSet resultSet, final RowMapper<T> rowMapper)
            throws SQLException {
        final List<T> results = new ArrayList<>();
        if (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        validate(resultSet, results);
        return results;
    }

    private <T> void validate(final ResultSet resultSet, final List<T> results) throws SQLException {
        if (resultSet.next()) {
            throw new SQLException("sql 결과 데이터가 2개 이상 존재합니다.");
        }
        if (results.isEmpty()) {
            throw new SQLException("sql 결과 데이터가 존재하지 않습니다.");
        }
    }
}
