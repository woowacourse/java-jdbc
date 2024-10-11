package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

    private DataSource dataSource;

    private final ResultMapper resultMapper;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.resultMapper = new ResultMapper();
    }

    public void update(final String sql, final Object... values) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(values);
            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 에러가 발생했습니다.", e);
        }
    }

    public void update(final Connection connection, final String sql, final Object... values) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(values);
            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 에러가 발생했습니다.", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return resultMapper.getResults(preparedStatement.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 에러가 발생했습니다.", e);
        }
    }

    public <T> Optional<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... values
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(values);
            preparedStatementSetter.setValues(preparedStatement);
            return resultMapper.findResult(preparedStatement.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 에러가 발생했습니다.", e);
        }
    }
}
