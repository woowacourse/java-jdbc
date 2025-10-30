package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public int update(String sql, Object... args) {
        return execute(sql, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            return pstmt.executeUpdate();
        });
    }

    public void update(Connection conn, String sql, Object... args) {
        execute(conn, sql, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            log.debug("query: {}", sql);
            pss.setValues(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        return execute(conn, sql, pstmt -> {
            log.debug("query: {}", sql);
            pss.setValues(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper, Object... args) {
        return query(conn, sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new RuntimeException("Result가 여러 개 입니다." + results.size());
        }
        return results.get(0);
    }

    public <T> T queryForObject(Connection conn, String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(conn, sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("Result가 여러 개 입니다." + results.size());
        }
        return results.get(0);
    }

    private <T> T execute(Connection conn, String sql, PreparedStatementCallback<T> action) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query: {}", sql);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection()) {
            return execute(conn, sql, action);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
