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

    public void update(final String sql, final Object... objects) {
        execute(sql, PreparedStatement::executeUpdate, objects);
    }

    public void update(final Connection connection, final String sql, final Object... objects) {
        execute(connection, sql, PreparedStatement::executeUpdate, objects);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        final ResultSetExtractor<Optional<T>> rse = resultSet -> {
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapToRow(resultSet));
            }
            return Optional.empty();
        };
        return query(sql, rse, objects);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        final ResultSetExtractor<List<T>> rse = resultSet -> {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapToRow(resultSet));
            }
            return results;
        };
        return query(sql, rse, objects);
    }

    private <T> T query(final String sql, final ResultSetExtractor<T> rse, final Object... elements) {
        final PreparedStatementExecutor<T> pse = preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return rse.extract(resultSet);
            }
        };
        return execute(sql, pse, elements);
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementExecutor<T> executor,
            final Object... objects
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setPreparedStatement(preparedStatement, objects);
            return executor.action(preparedStatement);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(
            final Connection connection,
            final String sql,
            final PreparedStatementExecutor<T> executor,
            final Object... objects
    ) {
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatement(preparedStatement, objects);
            return executor.action(preparedStatement);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatement(final PreparedStatement preparedStatement, final Object[] objects)
            throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
    }
}
