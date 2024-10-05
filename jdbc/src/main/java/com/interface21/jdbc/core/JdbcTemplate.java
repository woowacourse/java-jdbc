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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(sql, pstmt -> {
            setValues(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            setValues(pstmt, args);
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapRows(rs, rowMapper);
            }
        });
    }

    private void setValues(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> mapRows(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }

    private <T> T execute(String sql, PreparedStatementCallBack<T> callBack) {
        validateSql(sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return callBack.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void validateSql(String sql) {
        if (sql.isBlank()) {
            throw new IllegalArgumentException("sql must not be blank");
        }
        log.debug("Executing prepared SQL statement : [ {} ]", sql);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.getFirst();
    }
}
