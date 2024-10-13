package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

class SingleDataExtractor<T> implements DataExtractor<T, T> {

    @Override
    public T extract(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        if (resultSet.next()) {
            final T data = rowMapper.mapRow(resultSet);
            return getResult(resultSet, data);
        }
        throw new SQLException("sql 결과 데이터가 존재하지 않습니다.");
    }

    private T getResult(final ResultSet resultSet, final T data) throws SQLException {
        if (resultSet.next()) {
            throw new SQLException("sql 결과 데이터가 2개 이상 존재합니다.");
        }
        return data;
    }
}
