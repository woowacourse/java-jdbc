package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        executePreparedStatement(sql, preparedStatementSetter, PreparedStatement::executeUpdate);
    }

    public void update(String sql, Object... parameters) {
        update(sql, createDefaultPreparedStatementSetter(parameters));
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        List<T> result = query(sql, rowMapper, preparedStatementSetter);

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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        return executePreparedStatement(sql, preparedStatementSetter,
                preparedStatement -> extractResultSet(rowMapper, preparedStatement));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return query(sql, rowMapper, createDefaultPreparedStatementSetter(parameters));
    }

    private <T> T executePreparedStatement(String sql, PreparedStatementSetter setter, PreparedStatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            setter.setParameters(preparedStatement);

            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            return handleSQLException(e);
        }
    }

    private <T> List<T> extractResultSet(RowMapper<T> rowMapper, PreparedStatement preparedStatement) throws SQLException {
        List<T> result = new ArrayList<>();

        try (ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        }
    }

    private PreparedStatementSetter createDefaultPreparedStatementSetter(Object... parameters) {
        return preparedStatement -> {
            for (int i = 1; i <= parameters.length; i++) {
                preparedStatement.setObject(i, parameters[i - 1]);
            }
        };
    }

    private <T> T handleSQLException(SQLException e) {
        log.error(e.getMessage(), e);
        throw new DataAccessException(e);
    }
}
