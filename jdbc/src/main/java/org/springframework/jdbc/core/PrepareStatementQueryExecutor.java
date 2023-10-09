package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrepareStatementQueryExecutor<T> implements PrepareStatementExecutor<List<T>> {
    private final RowMapper<T> rowMapper;

    public PrepareStatementQueryExecutor(final RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> execute(final PreparedStatement preparedStatement) throws SQLException {
        final List<T> result = new ArrayList<>();
        final ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }
}
