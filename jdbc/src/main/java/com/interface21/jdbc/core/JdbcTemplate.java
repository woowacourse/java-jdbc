package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeException;
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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... parameters) {
        execute(PreparedStatement::executeUpdate, sql, parameters);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return execute((preparedStatement) -> executeQuery(preparedStatement, rowMapper), sql, parameters);
    }

    private <T> List<T> executeQuery(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);
        return extractSingleResult(results);
    }

    private <T> T execute(PreparedStatementCallback<T> preparedStatementCallback, String sql, Object... parameters) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                log.debug("query : {}", sql);
                setParameters(preparedStatement, parameters);
                return preparedStatementCallback.doInPreparedStatement(preparedStatement);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> Optional<T> extractSingleResult(List<T> results) {
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() != 1) {
            throw new IncorrectResultSizeException(1, results.size());
        }
        return Optional.ofNullable(results.getFirst());
    }

    private void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }
}
