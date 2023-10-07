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

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        final PreparedStatementExecutor<Optional<T>> pse = preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(rowMapper.mapToRow(resultSet));
                }
                return Optional.empty();
            }
        };
        return execute(sql, pse, objects);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        final PreparedStatementExecutor<List<T>> pse = preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapToRow(resultSet));
                }
                return results;
            }
        };
        return execute(sql, pse, objects);
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

    private void setPreparedStatement(final PreparedStatement preparedStatement, final Object[] objects)
            throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
    }
}
