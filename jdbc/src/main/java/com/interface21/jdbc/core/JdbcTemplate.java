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
        return execute(sql, ps -> executeUpdate(ps, pss));
    }

    private PreparedStatementSetter getDefaultPreparedStatementSetter(Object[] params) {
        return new OrderBasedPreparedStatementSetter(params);
    }

    private <T> T execute(String sql, SQLFunction<PreparedStatement, T> function) {
        log.debug("query : {}", sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return function.apply(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private int executeUpdate(PreparedStatement ps, PreparedStatementSetter pss) throws SQLException {
        pss.setValues(ps);
        return ps.executeUpdate();
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
        return execute(sql, ps -> executeQuery(ps, pss, rowMapper));
    }

    private <T> void validateResultUniqueness(List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(UNIQUE_SIZE);
        }
        if (results.size() > UNIQUE_SIZE) {
            throw new IncorrectResultSizeDataAccessException(UNIQUE_SIZE, results.size());
        }
    }

    private <T> List<T> executeQuery(PreparedStatement ps, PreparedStatementSetter pss, RowMapper<T> rowMapper)
            throws SQLException {
        pss.setValues(ps);
        try (ResultSet rs = ps.executeQuery()) {
            return getResults(rs, rowMapper);
        }
    }

    private <T> List<T> getResults(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForList(sql, rowMapper, getDefaultPreparedStatementSetter(params));
    }
}
