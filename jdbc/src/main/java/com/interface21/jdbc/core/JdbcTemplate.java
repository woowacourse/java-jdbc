package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            log.info("SQL : {}", sql);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs, 1);
                }
            }
        } catch (SQLException e) {
            log.error("SQL error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public int update(String sql, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            log.info("SQL : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
