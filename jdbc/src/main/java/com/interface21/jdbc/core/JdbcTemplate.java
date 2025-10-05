package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.util.Optional;
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

    public void update(final String sql) {
        execute(sql, PreparedStatement::executeUpdate, null);
    }

    public void update(final String sql, final PreparedStatementSetter setter) {
        execute(sql, PreparedStatement::executeUpdate, setter);
    }

    public <T> List<T> queryForObjects(final String sql, final RowMapper<T> rowMapper) {
        return queryForObjects(sql, rowMapper, null);
    }

    public <T> List<T> queryForObjects(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        }, setter);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                T result = rowMapper.mapRow(rs);

                if (rs.next()) {
                    throw new DataAccessException("Expected 0 or 1 result, but found more than 1");
                }

                return Optional.of(result);
            }
        }, setter);
    }

    public <T> T execute(final String sql, final PreparedStatementCallback<T> callback, final PreparedStatementSetter setter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            if (setter != null) {
                setter.setValues(pstmt);
            }
            return callback.execute(pstmt);
        } catch (SQLException e) {
            log.error("SQL execution failed: {}", sql, e);
            throw new DataAccessException(e);
        }
    }
}
