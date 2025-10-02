package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryForObject(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        List<T> results = query(sql, pss, rowMapper);
        return getSingleResult(results);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            return extractResults(pstmt.executeQuery(), rowMapper);
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("Executing SQL: {}", sql);

            return callback.doInPreparedStatement(pstmt);

        } catch (SQLException e) {
            log.error("SQL execution failed: {}", sql, e);
            throw new DataAccessException("SQL execution failed: " + sql, e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... args) {
        return ps -> setParameters(ps, args);
    }

    private void setParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> extractResults(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        try (rs) {
            List<T> results = new ArrayList<>();
            int rowNum = 0;

            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }

            return results;
        }
    }

    private <T> T getSingleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException("Query returned no results");
        }

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }

        return results.getFirst();
    }
}
