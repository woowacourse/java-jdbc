package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        final var result = execute(sql, preparedStatement -> {
            final var resultSet = preparedStatement.executeQuery();
            final var results = new ArrayList<T>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }, objects);
        return result.stream()
                .findAny();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, preparedStatement -> {
            final var resultSet = preparedStatement.executeQuery();
            final var results = new ArrayList<T>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        });
    }

    private PreparedStatement prepareStatement(final PreparedStatement preparedStatement, final Object[] objects)
            throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
        return preparedStatement;
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback,
                          final Object... objects) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = prepareStatement(connection.prepareStatement(sql), objects)) {
            log.debug("query : {}", sql);
            return preparedStatementCallback.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
