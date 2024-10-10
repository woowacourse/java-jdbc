package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql) {
        final LineCallback<Integer> callback = (connection, query) -> {
            final PreparedStatement statement = connection.prepareStatement(query);
            return statement.executeUpdate();
        };

        queryTemplate(sql, callback);
    }

    public void update(final String sql, final Object... bind) {
        final LineCallback<Integer> callback = (connection, query) -> {
            final PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 1; i <= bind.length; i++) {
                statement.setObject(i, bind[i - 1]);
            }
            return statement.executeUpdate();
        };
        queryTemplate(sql, callback);
    }

    public <T> T queryForObject(final String sql,
                                final RowMapStrategy<T> rowMapStrategy,
                                final Object... bind) {
        final LineCallback<ResultSet> callback = (connection, query) -> {
            final PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 1; i <= bind.length; i++) {
                statement.setObject(i, bind[i - 1]);
            }
            return statement.executeQuery();
        };
        return queryTemplate(sql, callback, rowMapStrategy).getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapStrategy<T> rowMapStrategy) {
        final LineCallback<ResultSet> callback = (connection, query) -> {
            final PreparedStatement statement = connection.prepareStatement(sql);
            return statement.executeQuery();
        };
        return queryTemplate(sql, callback, rowMapStrategy);
    }


    public int queryTemplate(final String sql, final LineCallback<Integer> callback) {
        try (final Connection connection = dataSource.getConnection()) {
            return callback.callback(connection, sql);
        } catch (final SQLException exception) {
            log.warn("SQL 쿼리 중 에외가 발생했습니다. : {}", exception.getMessage());
            throw new IllegalStateException("SQL 쿼리 중 예외가 발생했습니다.");
        }
    }

    public <T> List<T> queryTemplate(final String sql,
                                     final LineCallback<ResultSet> callback,
                                     final RowMapStrategy<T> strategy) {
        try (final Connection connection = dataSource.getConnection()) {
            final ResultSet resultSet = callback.callback(connection, sql);
            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                final T columnObject = strategy.mapRow(resultSet);
                result.add(columnObject);
            }
            return result;
        } catch (final SQLException exception) {
            log.warn("SQL 쿼리 중 에외가 발생했습니다. : {}", exception.getMessage());
            throw new IllegalStateException("SQL 쿼리 중 예외가 발생했습니다.");
        }
    }
}
