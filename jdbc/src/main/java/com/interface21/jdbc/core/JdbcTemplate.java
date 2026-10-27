package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bindParameters(statement, args);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute update: " + sql, e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bindParameters(statement, args);
            try (ResultSet resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, results.size()));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query: " + sql, e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, but got " + results.size());
        }
        return results.isEmpty() ? null : results.getFirst();
    }

    private void bindParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
