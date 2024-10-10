package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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
    private static final int UNIQUE_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... parameters) {
        return update(sql, getDefaultPreparedStatementSetter(parameters));
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, PreparedStatement::executeUpdate, preparedStatementSetter);
    }

    private PreparedStatementSetter getDefaultPreparedStatementSetter(Object[] parameters) {
        return new OrderBasedPreparedStatementSetter(parameters);
    }

    private <T> T execute(
            String sql,
            PreparedStatementCallback<T> callback,
            PreparedStatementSetter preparedStatementSetter
    ) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            return callback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return queryForObject(sql, rowMapper, getDefaultPreparedStatementSetter(parameters));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        List<T> results = queryForList(sql, rowMapper, preparedStatementSetter);
        validateResultUniqueness(results);
        return results.getFirst();
    }

    public <T> List<T> queryForList(
            String sql,
            RowMapper<T> rowMapper,
            PreparedStatementSetter preparedStatementSetter
    ) {
        return execute(sql, preparedStatement -> executeQuery(preparedStatement, rowMapper), preparedStatementSetter);
    }

    private <T> void validateResultUniqueness(List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(UNIQUE_SIZE);
        }
        if (results.size() > UNIQUE_SIZE) {
            throw new IncorrectResultSizeDataAccessException(UNIQUE_SIZE, results.size());
        }
    }

    private <T> List<T> executeQuery(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return getResults(resultSet, rowMapper);
        }
    }

    private <T> List<T> getResults(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return queryForList(sql, rowMapper, getDefaultPreparedStatementSetter(parameters));
    }
}
