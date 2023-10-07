package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrepareStatementQueryExecutor<T> implements PrepareStatementExecutor<List<T>> {

    private final RowMapper<T> rowMapper;

    public PrepareStatementQueryExecutor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> execute(PreparedStatement preparedStatement) throws SQLException {
        List<T> result = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }
}
