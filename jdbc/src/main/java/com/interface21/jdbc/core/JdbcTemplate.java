package com.interface21.jdbc.core;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.core.utils.DefaultDataExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.interface21.jdbc.core.utils.DefaultPreparedStatementSetterFactory.createDefaultPreparedStatementSetter;
import static com.interface21.jdbc.core.utils.SQLExceptionHandler.handleSQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter setter) {
        executePreparedStatement(sql, setter, PreparedStatement::executeUpdate);
    }

    public void update(String sql, Object... parameters) {
        update(sql, createDefaultPreparedStatementSetter(parameters));
    }

    public void update(Connection connection, String sql, PreparedStatementSetter setter) {
        executePreparedStatement(connection, sql, setter, PreparedStatement::executeUpdate);
    }

    public void update(Connection connection, String sql, Object... parameters) {
        update(connection, sql, createDefaultPreparedStatementSetter(parameters));
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        List<T> result = query(sql, rowMapper, setter);

        if (result.size() > 1) {
            throw new IncorrectResultSizeDataAccessException();
        }

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.getFirst());
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return queryForObject(sql, rowMapper, createDefaultPreparedStatementSetter(parameters));
    }

    public <T> List<T> query(String sql, PreparedStatementSetter setter, ResultSetDataExtractor<T> extractor) {
        return executePreparedStatement(sql, setter,
                extractor::extractData);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return query(sql, createDefaultPreparedStatementSetter(parameters), new DefaultDataExtractor<>(rowMapper));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        return query(sql, setter, new DefaultDataExtractor<>(rowMapper));
    }

    public <T> List<T> query(String sql, ResultSetDataExtractor<T> extractor, Object... parameters) {
        return query(sql, createDefaultPreparedStatementSetter(parameters), extractor);
    }

    private <T> T executePreparedStatement(String sql, PreparedStatementSetter setter, PreparedStatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection()) {
            return executePreparedStatement(conn, sql, setter, callback);
        } catch (SQLException e) {
            return handleSQLException(e);
        }
    }

    private <T> T executePreparedStatement(Connection conn, String sql, PreparedStatementSetter setter, PreparedStatementCallback<T> callback) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setParameters(preparedStatement);

            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            return handleSQLException(e);
        }
    }
}
