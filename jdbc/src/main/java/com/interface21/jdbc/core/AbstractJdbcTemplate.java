package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcTemplate.class);

    public final int update() {
        final var sql = createQuery();
        log.debug("query : {}", sql);
        try (final var connection = getDataSource().getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setValues(statement);
            return statement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    public final Object query() {
        final var sql = createQuery();
        log.debug("query : {}", sql);
        try (final var connection = getDataSource().getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setValues(statement);
            try (final var resultSet = statement.executeQuery()) {
                return mapRow(resultSet);
            }
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract void setValues(PreparedStatement statement) throws SQLException;

    protected abstract Object mapRow(ResultSet resultSet) throws SQLException;
}
