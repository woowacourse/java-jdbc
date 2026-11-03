package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcTemplate.class);

    public final int update(final PreparedStatementSetter setter) {
        final var sql = createQuery();
        log.debug("query : {}", sql);
        try (final var connection = getDataSource().getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return statement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    public final <T> List<T> query(final RowMapper<T> rowMapper) {
        return query(new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement statement) {
            }
        }, rowMapper);
    }

    public final <T> List<T> query(final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        final var sql = createQuery();
        log.debug("query : {}", sql);
        try (final var connection = getDataSource().getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            try (final var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();
}
