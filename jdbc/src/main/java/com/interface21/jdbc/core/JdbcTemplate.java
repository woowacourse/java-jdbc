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

    public int update(String sql, Object... params) {
        return update(sql, getDefaultPreparedStatementSetter(params));
    }

    public int update(String sql, PreparedStatementSetter pss) {
        log.debug("query : {}", sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            pss.setValues(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter getDefaultPreparedStatementSetter(Object[] params) {
        return new OrderBasedPreparedStatementSetter(params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(sql, rowMapper, getDefaultPreparedStatementSetter(params));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        List<T> results = queryForList(sql, rowMapper, pss);
        validateResultUniqueness(results);
        return results.getFirst();
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        log.debug("query : {}", sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(ps, pss)) {
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

    private ResultSet executeQuery(PreparedStatement ps, PreparedStatementSetter pss) throws SQLException {
        pss.setValues(ps);
        return ps.executeQuery();
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForList(sql, rowMapper, getDefaultPreparedStatementSetter(params));
    }
}
