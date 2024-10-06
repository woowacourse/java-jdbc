package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(
                sql,
                pstmt -> {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<T> results = new ArrayList<>();
                        while (rs.next()) {
                            results.add(rowMapper.map(rs));
                        }
                        return results;
                    }
                },
                params
        );
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        if (results.size() > 1) {
            throw new IncorrectResultSizeException(1, results.size());
        }
        return results.getFirst();
    }

    public int update(String sql, Object... params) {
        return execute(sql, PreparedStatement::executeUpdate, params);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
