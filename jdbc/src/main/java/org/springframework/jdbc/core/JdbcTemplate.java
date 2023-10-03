package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... elements) {
        execute(sql, PreparedStatement::execute, elements);
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementExecutor<T> executor,
            final Object... elements
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setElements(elements, preparedStatement);

            return executor.action(preparedStatement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setElements(final Object[] elements, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < elements.length; i += 1) {
            preparedStatement.setObject(i + 1, elements[i]);
        }
    }

    public <T> Optional<T> queryForObject(
            final RowMapper<T> rowMapper,
            final String sql,
            final Object... elements
    ) {
        return execute(sql, preparedStatement -> createResult(preparedStatement, rowMapper), elements);
    }

    private <T> Optional<T> createResult(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            return Optional.empty();
        }
    }

    public <T> List<T> query(
            final RowMapper<T> rowMapper,
            final String sql,
            final Object... elements
    ) {
        return execute(sql, preparedStatement -> createResults(preparedStatement, rowMapper), elements);
    }

    private <T> List<T> createResults(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }
    }

}
