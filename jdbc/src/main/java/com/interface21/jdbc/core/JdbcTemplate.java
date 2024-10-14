package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST_PARAMETER_INDEX = 1;
    private static final int SINGLE_RESULT_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForOptional(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = queryForList(sql, rowMapper, args);
        return Optional.ofNullable(singleResult(results));
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... args)
            throws DataAccessException {
        return execute(sql, statement -> query(rowMapper, statement), args);
    }

    private <T> List<T> query(final RowMapper<T> rowMapper, final PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return extractData(rowMapper, resultSet);
        }
    }

    private <T> List<T> extractData(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            T result = rowMapper.mapRow(resultSet);
            results.add(result);
        }
        return results;
    }

    private <T> T singleResult(List<T> results) throws IncorrectResultSizeDataAccessException {
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > SINGLE_RESULT_SIZE) {
            throw new IncorrectResultSizeDataAccessException(SINGLE_RESULT_SIZE, results.size());
        }
        return results.getFirst();
    }

    public int update(final String sql, final Object... args) throws DataAccessException {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> action, final Object... args) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setPreparedStatementArgs(statement, args);
            return action.doInPreparedStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            releaseConnectionIfAutoCommit(connection);
        }
    }

    private void releaseConnectionIfAutoCommit(Connection connection) {
        try {
            if (connection.getAutoCommit()) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatementArgs(final PreparedStatement statement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(FIRST_PARAMETER_INDEX + i, args[i]);
        }
    }
}
