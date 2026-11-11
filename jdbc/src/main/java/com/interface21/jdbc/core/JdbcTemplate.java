package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> mapper, final Object... args) {
        return singleResult(query(sql, mapper, args));
    }

    private <T> T singleResult(final List<T> results) {
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IllegalStateException("Expected at most one row, but found " + results.size());
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final Object... args) {
        return query(sql, mapper, argumentsSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final PreparedStatementSetter setter) {
        return withConnection(connection -> query(connection, sql, mapper, setter));
    }

    private <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> mapper, final PreparedStatementSetter setter) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);

            try (ResultSet rs = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public int update(final String sql, final Object... args) {
        return update(sql, argumentsSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return withConnection(connection -> update(connection, sql, setter));
    }

    private int update(final Connection connection, final String sql, final PreparedStatementSetter setter) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T withConnection(final Function<Connection, T> operation) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        final T result;
        try {
            result = operation.apply(connection);
        } catch (RuntimeException | Error failure) {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException releaseFailure) {
                failure.addSuppressed(releaseFailure);
            }
            throw failure;
        }
        DataSourceUtils.releaseConnection(connection, dataSource);
        return result;
    }

    private PreparedStatementSetter argumentsSetter(final Object... args) {
        return statement -> {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        };
    }

}
