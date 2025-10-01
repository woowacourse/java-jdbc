package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(
            final String sql,
            final Object... args
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public <T> List<T> findAll(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            try (final var resultSet = preparedStatement.executeQuery()) {
                final var results = new java.util.ArrayList<T>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (final SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public <T> T findById(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            try (final var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet);
                }
                return null;
            }
        } catch (final SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            try (final var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet);
                }
                return null;
            }
        } catch (final SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public int update(
            final String sql,
            final Object... args
    ) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}
