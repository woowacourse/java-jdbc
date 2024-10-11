package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

    private final DataSource dataSource;
    private final ParameterBinder parameterBinder;
    private final ResultMapper resultMapper;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.parameterBinder = new ParameterBinder();
        this.resultMapper = new ResultMapper();
    }

    public void write(String sql, Object... args) {
        executeQuery(sql, preparedStatement -> {
            preparedStatement.executeUpdate();
            return null;
        }, args);
    }

    public <T> Optional<T> read(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(sql, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultMapper.findResult(resultSet, rowMapper);
        }, args);
    }

    public <T> List<T> readAll(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(sql, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultMapper.getResults(resultSet, rowMapper);
        }, args);
    }

    private <T> T executeQuery(String sql, QueryExecution<PreparedStatement, T> query, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            parameterBinder.bindParameters(preparedStatement, args);
            return query.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("쿼리 실행 도중 에러가 발생했습니다.", e);
        }
    }
}
