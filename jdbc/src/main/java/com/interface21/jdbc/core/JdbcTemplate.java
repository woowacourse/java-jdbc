package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int UNIQUE_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... params) {
        log.debug("query : {}", sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = queryForList(sql, rowMapper, params);
        validateResultUniqueness(results);
        return results.getFirst();
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        log.debug("query : {}", sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(ps, params)) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> void validateResultUniqueness(List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(UNIQUE_SIZE);
        }
        if (results.size() > UNIQUE_SIZE) {
            throw new IncorrectResultSizeDataAccessException(UNIQUE_SIZE, results.size());
        }
    }

    private ResultSet executeQuery(PreparedStatement ps, Object... params) throws SQLException {
        setParams(ps, params);
        return ps.executeQuery();
    }
}
