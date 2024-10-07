package com.interface21.jdbc.core;

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

import com.interface21.jdbc.exception.DatabaseException;
import com.interface21.jdbc.exception.UnexpectedResultSizeException;

public class JdbcTemplate {
    private static final int BASE_INDEX = 1;

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setValues(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DatabaseException("Database error occurred while executing query.", e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, Object... params) {
        List<T> resultSet = query(sql, rowMapper, params);
        if (resultSet.size() > 1) {
            throw new UnexpectedResultSizeException("Multiple results returned for query, but only one result expected.");
        }

        return resultSet.stream().findFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setValues(pstmt, params);
            return executeQuery(pstmt, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DatabaseException("Database error occurred while executing query.", e);
        }
    }

    private void setValues(final PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + BASE_INDEX, params[i]);
        }
    }

    private <T> List<T> executeQuery(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }
    }
}
