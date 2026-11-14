package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, statement -> {}, rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                           final RowMapper<T> rowMapper) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);

            try (final var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter,
                               final RowMapper<T> rowMapper) {
        final var results = query(sql, setter, rowMapper);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, found " + results.size());
        }
        return results.isEmpty() ? null : results.get(0);
    }
}
