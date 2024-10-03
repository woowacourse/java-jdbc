package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return execute(sql, pstmt -> {
            setObject(pstmt, parameters);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            }
            return null;
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return execute(sql, pstmt -> {
            setObject(pstmt, parameters);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }
        });
    }

    public void update(String sql, Object... parameters) {
        execute(sql, pstmt -> {
            setObject(pstmt, parameters);
            return pstmt.executeUpdate();
        });
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return executor.execute(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setObject(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }
}
