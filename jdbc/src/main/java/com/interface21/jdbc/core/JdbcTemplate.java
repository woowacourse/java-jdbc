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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... params) {
        return executeWithPreparedStatement(sql, (pstmt -> {
            settingPrepareStatement(params, pstmt);

            return pstmt.executeUpdate();
        }));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return executeWithPreparedStatement(sql, (pstmt -> {
            settingPrepareStatement(params, pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return rowMapper.map(rs);
                }
                return null;
            }
        }));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return executeWithPreparedStatement(sql, (pstmt -> {
            settingPrepareStatement(params, pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(rowMapper.map(rs));
                }

                return results;
            }
        }));
    }

    private <T> T executeWithPreparedStatement(String sql, PreparedStatementAction<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void settingPrepareStatement(final Object[] params, final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
