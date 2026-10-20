package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public void update(final String sql, final Object... args) {
        log.debug("query : {}", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, args);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute update", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        log.debug("query : {}", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, args);

            try (final ResultSet resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    private void setParameters(final PreparedStatement statement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
