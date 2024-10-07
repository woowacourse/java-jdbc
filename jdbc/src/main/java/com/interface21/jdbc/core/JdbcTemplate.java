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
            log.error(e.getMessage(), e);
            throw new CannotGetJdbcConnectionException(e.getMessage(), e);
        }
    }

    public void update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatement(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, params);
             ResultSet rs = executeQuery(pstmt)) {

            List<T> results = mapResults(rs, rowMapper);
            validateSingleResult(results);

            return results.get(0);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement createPreparedStatement(Connection conn, String sql, Object... params)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        setStatement(pstmt, params);
        return pstmt;
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    private <T> void validateSingleResult(List<T> result) throws SQLException {
        if (result.size() != 1) {
            System.out.println(result);
            throw new IncorrectResultSizeDataAccessException("Incorrect result size");
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, params);
             ResultSet rs = executeQuery(pstmt)) {

            return mapResults(rs, rowMapper);

        } catch (SQLException e) {
            log.error("Error during query execution.", e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> mapResults(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();

        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }

        if (results.isEmpty()) {
            throw new NoSuchElementException();
        }

        return results;
    }


    private void setStatement(PreparedStatement pstmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    public void execute(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
