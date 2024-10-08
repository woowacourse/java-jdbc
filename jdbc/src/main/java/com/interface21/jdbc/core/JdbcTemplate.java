package com.interface21.jdbc.core;

import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;
    private final SqlExecutor sqlExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.sqlExecutor = new SqlExecutor();
    }

    public void executeUpdate(String sql, Object... parameters) {
        PreparedStatementExecutor<Integer> statementExecutor = PreparedStatement::executeUpdate;
        sqlExecutor.execute(sql, dataSource, statementExecutor, parameters);
    }

    public <T> T fetchResult(String sql, ResultMapper<T> resultMapper, Object... parameters) {
        List<T> results = fetchResults(sql, resultMapper, parameters);
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.getFirst();
    }

    public <T> List<T> fetchResults(String sql, ResultMapper<T> resultMapper, Object... parameters) {
        PreparedStatementExecutor<List<T>> statementExecutor = preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return mapResults(resultSet, resultMapper);
            }
        };
        return sqlExecutor.execute(sql, dataSource, statementExecutor, parameters);
    }

    private <T> List<T> mapResults(ResultSet resultSet, ResultMapper<T> resultMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(resultMapper.mapResult(resultSet));
        }
        return results;
    }
}
