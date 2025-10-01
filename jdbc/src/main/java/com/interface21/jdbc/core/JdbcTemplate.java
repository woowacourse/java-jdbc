package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
                final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, args)
        ) {
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
                final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, args);
                final ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            return mapResultSetToList(resultSet, rowMapper);
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
                final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, args);
                final ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            return mapResultSetToObject(resultSet, rowMapper);
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
                final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, args);
                final ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            return mapResultSetToObject(resultSet, rowMapper);
        } catch (final SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public int update(
            final String sql,
            final Object... args
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, args)
        ) {
            return preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    private PreparedStatement createPreparedStatement(
            final Connection con,
            final String sql,
            final Object... args
    )
            throws SQLException {
        final PreparedStatement ps = con.prepareStatement(sql);
        log.debug("query : {}", sql);
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
        return ps;
    }

    private <T> List<T> mapResultSetToList(
            final ResultSet resultSet,
            final RowMapper<T> rowMapper
    ) throws SQLException {
        final var results = new ArrayList<T>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private <T> T mapResultSetToObject(
            final ResultSet resultSet,
            final RowMapper<T> rowMapper
    ) throws SQLException {
        if (resultSet.next()) {
            return rowMapper.mapRow(resultSet);
        }
        return null;
    }
}
