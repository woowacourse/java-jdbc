package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("Failed to get connection", e);
            throw new CannotGetJdbcConnectionException("Unable to get a connection", e);
        }
    }

    public void update(String sql, Object... params) {
        executeUpdate(sql, params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        validateSingleResult(results);
        return results.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params);
             ResultSet rs = pstmt.executeQuery()) {

            return mapResults(rs, rowMapper);

        } catch (SQLException e) {
            log.error("Error during query execution.", e);
            throw new DataAccessException("Failed to execute query", e);
        }
    }

    public void execute(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error("Execution failed", e);
            throw new DataAccessException("Failed to execute statement", e);
        }
    }

    private void executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Update failed", e);
            throw new DataAccessException("Failed to execute update", e);
        }
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ArgumentPreparedStatementSetter argSetter = new ArgumentPreparedStatementSetter(args);
        argSetter.setValues(pstmt);
        return pstmt;
    }

    private <T> List<T> mapResults(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        if (results.isEmpty()) {
            throw new NoSuchElementException("No results found");
        }
        return results;
    }

    private <T> void validateSingleResult(List<T> result) {
        if (result.size() != 1) {
            throw new IncorrectResultSizeDataAccessException("Expected one result, but got " + result.size());
        }
    }
}
