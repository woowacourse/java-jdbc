package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final PreparedStatementStrategy strategy) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = strategy.makePreparedStatement(connection)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("jdbc 쿼리 중 오류 발생했습니다.");
        }
    }

    public <T> T queryForObject(final PreparedStatementStrategy preparedStatementStrategy,
                                final RowMapStrategy<T> rowMapStrategy) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = preparedStatementStrategy.makePreparedStatement(connection);
             final ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return rowMapStrategy.mapRow(resultSet);
            }
        } catch (final SQLException e) {
            throw new IllegalStateException("jdbc 쿼리 중 오류 발생했습니다.");
        }
        return null;
    }

    public <T> List<T> query(final PreparedStatementStrategy preparedStatementStrategy,
                             final RowMapStrategy<T> rowMapStrategy) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = preparedStatementStrategy.makePreparedStatement(connection);
             final ResultSet resultSet = statement.executeQuery()) {
            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapStrategy.mapRow(resultSet));
            }
            return result;
        } catch (final SQLException e) {
            throw new IllegalStateException("jdbc 쿼리 중 오류 발생했습니다.");
        }
    }
}
