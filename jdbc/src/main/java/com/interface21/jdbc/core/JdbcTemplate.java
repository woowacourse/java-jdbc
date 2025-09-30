package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void update(final String sql, final Object... parameters) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParameters(preparedStatement, parameters);

            preparedStatement.executeUpdate();
            log.info("query: {}", sql);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> results = queryForList(sql, rowMapper, parameters);

        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DataAccessException("Incorrect result size: expected 1, but got " + results.size());
        }
        return Optional.of(results.getFirst());
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParameters(preparedStatement, parameters);

            try (final var queryResultSet = preparedStatement.executeQuery()) {
                log.info("query: {}", sql);
                final var objectMappingResultSet = new ArrayList<T>();

                while (queryResultSet.next()) {
                    objectMappingResultSet.add(rowMapper.mapRow(queryResultSet));
                }
                return objectMappingResultSet;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object[] parameters)
            throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }
}
