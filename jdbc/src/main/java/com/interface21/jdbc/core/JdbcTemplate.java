package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int START_PARAMETER_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... parameters) {
        return execute(sql, preparedStatement -> {
            setParameters(preparedStatement, parameters);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return execute(sql, (preparedStatement) -> {
            setParameters(preparedStatement, parameters);
            ResultSet rs = preparedStatement.executeQuery();
            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rowMapper.map(rs));
            }
            return results;
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return execute(sql, (preparedStatement) -> {
            setParameters(preparedStatement, parameters);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return rowMapper.map(resultSet);
            }
            return null;
        });
    }

    private void setParameters(PreparedStatement preparedStatement, Object... parameters)
            throws SQLException {
        int parameterIndex = START_PARAMETER_INDEX;
        for (Object parameter: parameters) {
            preparedStatement.setObject(parameterIndex, parameter);
            parameterIndex++;
        }
    }

    public <T> T execute(String sql, SqlFunction<PreparedStatement, T> action) {
        validateSql(sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            log.debug("Executing query: {}", sql);

            return action.apply(preparedStatement);
        } catch (SQLException e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void validateSql(String sql) {
        // 예: SQL 예약어가 포함되어 있는지 확인
        if (sql.toLowerCase().contains("drop") || sql.toLowerCase().contains("delete")) {
            throw new IllegalArgumentException("Unsafe SQL query detected.");
        }
    }
}
