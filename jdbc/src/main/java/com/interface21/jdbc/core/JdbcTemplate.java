package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return update(sql, createPreparedStatementSetter(args));
    }

    public int update(String sql, PreparedStatementSetter pss) {
        return execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public int update(Connection conn, String sql, Object... args) {
        return update(conn, sql, createPreparedStatementSetter(args));
    }

    public int update(Connection conn, String sql, PreparedStatementSetter pss) {
        return execute(conn, sql, pss, PreparedStatement::executeUpdate);
    }

    public <T> T execute(String sql, PreparedStatementSetter pss, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection()) {
            return execute(conn, sql, pss, action);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T execute(Connection conn, String sql, PreparedStatementSetter pss, PreparedStatementCallback<T> action) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pss.setValues(pstmt);

            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return execute(sql, pss, pstmt -> extractResults(pstmt, rowMapper));
    }

    public <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper, Object... args) {
        return query(conn, sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(Connection conn, String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return execute(conn, sql, pss, pstmt -> extractResults(pstmt, rowMapper));
    }

    private <T> List<T> extractResults(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            return mapRows(rs, rowMapper);
        }
    }

    private <T> List<T> mapRows(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("Result가 여러 개 입니다: " + results.size());
        }
        return results.get(0);
    }

    public <T> T queryForObject(Connection conn, String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(conn, sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("Result가 여러 개 입니다: " + results.size());
        }
        return results.get(0);
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }
}
