package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private final DataSource dataSource;
    private final ResultMapper resultMapper;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.resultMapper = new ResultMapper();
    }

    public void update(final String sql, final Object... values) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(values);
            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 에러가 발생했습니다.", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(values);
            preparedStatementSetter.setValues(preparedStatement);
            return resultMapper.findResult(preparedStatement.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 에러가 발생했습니다.", e);
        }
    }
}
