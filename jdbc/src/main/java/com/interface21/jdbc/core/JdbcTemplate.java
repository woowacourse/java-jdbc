package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public <T> List<T> query(Connection conn, String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return execute(conn, sql, ps -> {
            if (pss != null) {
                pss.setValues(ps);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.map(rs));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper, Object... params) {
        return query(conn, sql, new ArgumentPreparedStatementSetter(params), rowMapper);
    }

    public <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper) {
        return query(conn, sql, null, rowMapper);
    }

    public <T> T queryForObject(Connection conn, String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        List<T> results = query(conn, sql, pss, rowMapper);
        if (results.size() > 1) {
            throw new IncorrectResultSizeException(1, results.size());
        }
        return results.getFirst();
    }

    public <T> T queryForObject(Connection conn, String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(conn, sql, new ArgumentPreparedStatementSetter(params), rowMapper);
    }

    public <T> T queryForObject(Connection conn, String sql, RowMapper<T> rowMapper) {
        return queryForObject(conn, sql, null, rowMapper);
    }

    public int update(Connection conn, String sql, @Nullable PreparedStatementSetter pss) {
        return execute(conn, sql, ps -> {
            if (pss != null) {
                pss.setValues(ps);
            }
            return ps.executeUpdate();
        });
    }

    public int update(Connection conn, String sql, Object... params) {
        return update(conn, sql, new ArgumentPreparedStatementSetter(params));
    }

    private <T> T execute(Connection conn, String sql, PreparedStatementCallback<T> action) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
