package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

    public void update(final String sql, PreparedStatementSetter pss) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pss.setValues(pstmt);
            log.debug("query : {}", sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("JdbcTemplate {} failed. SQL: {}", "update", sql, e);
            throw new DataAccessException("JdbcTemplate query failed", e);
        }
    }

    public void update(final String sql, final Object...args) {
        update(sql, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pss.setValues(pstmt);
            log.debug("query : {}", sql);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            log.error("JdbcTemplate {} failed. SQL: {}", "queryForObject", sql, e);
            throw new DataAccessException("JdbcTemplate query failed", e);
        }
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, final Object...args) {
        return queryForObject(sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> List<T> queryForList(final String sql, RowMapper<T> rowMapper) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            try (ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }

                if (results.isEmpty()) {
                    return Collections.emptyList();
                }
                return results;
            }

        } catch (SQLException e) {
            log.error("JdbcTemplate {} failed. SQL: {}", "queryForList", sql, e);
            throw new DataAccessException("JdbcTemplate query failed", e);
        }
    }
}
