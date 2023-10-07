package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrepareStatementQueryForObjectExecutor<T> implements PrepareStatementExecutor<T> {

    private final RowMapper<T> rowMapper;

    public PrepareStatementQueryForObjectExecutor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public T execute(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        T result = null;
        while (resultSet.next()) {
            if (result != null) {
                throw new DataAccessException("1개 이상의 결과가 존재합니다.");
            }
            result = rowMapper.mapRow(resultSet);
        }
        return result;
    }
}
