package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, ParameterSetter parameterSetter) {
        return execute(sql, preparedStatement -> {
            parameterSetter.setParameters(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        return queryForList(sql, rowMapper, preparedStatement -> {});
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, ParameterSetter parameterSetter) {
        return execute(sql, (preparedStatement) -> {
            parameterSetter.setParameters(preparedStatement);
            return MappedResultSet.create(rowMapper, preparedStatement)
                    .getResults();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        return queryForObject(sql, rowMapper, preparedStatement -> {});
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, ParameterSetter parameterSetter) {
        return execute(sql, (preparedStatement) -> {
            parameterSetter.setParameters(preparedStatement);
            return MappedResultSet.create(rowMapper, preparedStatement, 1)
                    .getFirst();
        });
    }

    public <T> T execute(String sql, SqlFunction<PreparedStatement, T> action) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("Executing query: {}", sql);

            return action.apply(preparedStatement);
        } catch (SQLException e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
