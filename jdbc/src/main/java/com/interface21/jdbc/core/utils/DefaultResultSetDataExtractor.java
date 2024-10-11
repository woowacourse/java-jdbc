package com.interface21.jdbc.core.utils;

import com.interface21.jdbc.core.ResultSetDataExtractor;
import com.interface21.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefaultResultSetDataExtractor<T> implements ResultSetDataExtractor<T> {

    private final RowMapper<T> rowMapper;

    public DefaultResultSetDataExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> extractData(PreparedStatement preparedStatement) throws SQLException {
        List<T> result = new ArrayList<>();

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
        }
        return result;
    }
}
