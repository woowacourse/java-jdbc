package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

    private final DataSource dataSource;
    private final ResultMapper resultMapper;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.resultMapper = new ResultMapper();
    }

    public void update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        executeUpdate(
                sql,
                preparedStatementSetter);
    }

    public <T> List<T> readAll(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final RowMapper<T> rowMapper
    ) {
        return executeQuery(
                sql,
                preparedStatementSetter,
                preparedStatement -> resultMapper.getResults(preparedStatement.executeQuery(), rowMapper)
        );
    }

    public <T> Optional<T> query(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final RowMapper<T> rowMapper
    ) {
        return executeQuery(
                sql,
                preparedStatementSetter,
                preparedStatement -> resultMapper.findResult(preparedStatement.executeQuery(), rowMapper)
        );
    }

    private int executeUpdate(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 에러가 발생했습니다.", e);
        }
    }

    private <T> T executeQuery(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final QueryFunction<PreparedStatement, T> action
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            return action.apply(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 에러가 발생했습니다.", e);
        }
    }
}
