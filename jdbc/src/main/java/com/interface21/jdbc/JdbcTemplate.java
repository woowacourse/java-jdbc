package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;
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

    public void update(
            final String sql,
            final PreparedStatementSetter pss
    ) {
        execute(sql, pss, preparedStatement -> {
            preparedStatement.executeUpdate();
            return null;
        });
    }

    public void update(
            final Connection connection,
            final String sql,
            final PreparedStatementSetter pss
    ) {
        execute(connection, sql, pss, preparedStatement -> {
            preparedStatement.executeUpdate();
            return null;
        });
    }

    public void update(
            final String sql,
            final Object... args
    ) {
        update(sql, getDefaultPreparedStatementSetter(args));
    }

    public void update(
            final Connection connection,
            final String sql,
            final Object... args
    ) {
        update(connection, sql, getDefaultPreparedStatementSetter(args));
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter pss
    ) {
        return execute(sql, pss, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        });
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return query(sql, rowMapper, getDefaultPreparedStatementSetter(args));
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter pss
    ) {
        final List<T> results = query(sql, rowMapper, pss);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return queryForObject(sql, rowMapper, getDefaultPreparedStatementSetter(args));
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementSetter pss,
            final PreparedStatementExecutor<T> executor
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setValues(preparedStatement);
            return executor.execute(preparedStatement);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(
            final Connection connection,
            final String sql,
            final PreparedStatementSetter pss,
            final PreparedStatementExecutor<T> executor
    ) {
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setValues(preparedStatement);
            return executor.execute(preparedStatement);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter getDefaultPreparedStatementSetter(final Object[] args) {
        return preparedStatement -> {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementExecutor<T> {
        T execute(final PreparedStatement preparedStatement) throws SQLException;
    }
}
