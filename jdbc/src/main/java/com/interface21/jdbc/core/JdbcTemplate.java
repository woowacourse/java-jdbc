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
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, statement -> {
            final List<T> results = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
            }
            return results;
        }, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most 1 result, but found " + results.size());
        }
        return results.isEmpty() ? null : results.get(0);
    }

    private <T> T execute(final String sql, final StatementCallback<T> callback, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }

            return callback.doInStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private interface StatementCallback<T> {

        T doInStatement(PreparedStatement statement) throws SQLException;
    }
}
