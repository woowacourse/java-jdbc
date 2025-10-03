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

    public void update(final String sql, final Object... parameters) {
        execute(
                sql,
                preparedStatement -> {
                    preparedStatement.executeUpdate();
                    return null;
                },
                parameters
        );
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(
                sql,
                preparedStatement -> {
                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        return mapSingleResult(resultSet, rowMapper);
                    }
                },
                parameters
        );
    }

    private <T> T mapSingleResult(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        if (resultSet.next()) {
            return rowMapper.mapRow(resultSet, 1);
        }
        throw new SQLException("No data found");
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(
                sql,
                preparedStatement -> {
                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        return mapResult(resultSet, rowMapper);
                    }
                },
                parameters
        );
    }

    private <T> List<T> mapResult(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        int rowNum = 1;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementCallback<T> preparedStatementCallback,
            final Object... parameters
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setPreparedStatementParameters(preparedStatement, parameters);
            log.debug("query : {}", sql);

            return preparedStatementCallback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatementParameters(
            final PreparedStatement preparedStatement,
            final Object... parameters
    ) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }
}
