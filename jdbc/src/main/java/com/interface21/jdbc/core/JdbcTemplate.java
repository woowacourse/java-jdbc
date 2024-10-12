package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
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

    public <T> List<T> query(String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return execute(sql, ps -> {
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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return query(sql, new ArgumentPreparedStatementSetter(params), rowMapper);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(sql, null, rowMapper);
    }

    public <T> T queryForObject(String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        List<T> results = query(sql, rowMapper, pss);
        if (results.size() > 1) {
            throw new IncorrectResultSizeException(1, results.size());
        }
        return results.getFirst();
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(sql, new ArgumentPreparedStatementSetter(params), rowMapper);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        return queryForObject(sql, null, rowMapper);
    }

    public int update(String sql, @Nullable PreparedStatementSetter pss) {
        return execute(sql, ps -> {
            if (pss != null) {
                pss.setValues(ps);
            }
            return ps.executeUpdate();
        });
    }

    public int update(String sql, Object... params) {
        return update(sql, new ArgumentPreparedStatementSetter(params));
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
