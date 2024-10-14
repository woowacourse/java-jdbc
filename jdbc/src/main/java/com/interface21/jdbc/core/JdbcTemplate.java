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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, ParameterSetter parameterSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            Parameters parameters = parameterSetter.createParameters();
            parameters.setPreparedStatement(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new JdbcTemplateException("Cannot access database and connection or invalid sql query : " + sql, exception);
        }
    }

    public <T> T queryForObject(String sql, ParameterSetter parameterSetter, RowMapper<T> rowMapper) {
        List<T> result = executeQueryWithParams(sql, parameterSetter.createParameters(), rowMapper);
        return result.isEmpty() ? null : result.get(0);
    }

    public <T> List<T> query(String sql, Parameters parameters, RowMapper<T> rowMapper) {
        return executeQueryWithParams(sql, parameters, rowMapper);
    }

    private <T> List<T> executeQueryWithParams(String sql, Parameters parameters, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            parameters.setPreparedStatement(pstmt);
            return executeQuery(pstmt, rowMapper);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new JdbcTemplateException(
                    "Cannot access database and connection or invalid SQL query: " + sql, exception);
        }
    }

    private <T> List<T> executeQuery(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }
}
