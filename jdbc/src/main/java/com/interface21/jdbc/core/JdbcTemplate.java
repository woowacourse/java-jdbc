package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final int MAX_ALLOWED_RESULTS = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setSQLParams(statement, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error insert sql: " + sql, e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> result = query(sql, rowMapper, params);
        if (result.isEmpty()) {
            throw new DataAccessException("Expected a single result, but not found for query: " + sql);
        }
        if (result.size() > MAX_ALLOWED_RESULTS) {
            throw new DataAccessException("Expected a single result, but found multiple for query: " + sql);
        }
        return result.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setSQLParams(statement, params);
            return queryForAll(statement, rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException("Error insert sql: " + sql, e);
        }
    }

    private <T> List<T> queryForAll(PreparedStatement statement, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        }
    }

    private void setSQLParams(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
