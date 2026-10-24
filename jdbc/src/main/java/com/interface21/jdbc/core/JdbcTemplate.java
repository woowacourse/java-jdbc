package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DuplicateKeyException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final String DUPLICATE_KEY_SQL_STATE = "23505";

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                             final RowMapper<T> rowMapper) {
        return execute(sql, setter, pstmt -> {
            try (var rs = pstmt.executeQuery()) {
                final var results = new ArrayList<T>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter,
                                final RowMapper<T> rowMapper) {
        return execute(sql, setter, pstmt -> {
            try (var rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                final var result = rowMapper.mapRow(rs);
                if (rs.next()) {
                    throw new IncorrectResultSizeDataAccessException(1, 2);
                }
                return result;
            }
        });
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final StatementCallback<T> callback) {
        try (var conn = dataSource.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(pstmt);
            return callback.doInStatement(pstmt);
        } catch (SQLException e) {
            throw translate(sql, e);
        }
    }

    private DataAccessException translate(final String sql, final SQLException e) {
        if (DUPLICATE_KEY_SQL_STATE.equals(e.getSQLState())) {
            return new DuplicateKeyException("Duplicate key for SQL [" + sql + "]", e);
        }
        return new DataAccessException("Failed to execute SQL [" + sql + "]", e);
    }
}
