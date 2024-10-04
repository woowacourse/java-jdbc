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
    public static final int START_ARGUMENT_COUNT = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... arguments) {
        log.debug("update query : {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int count = 1;
            for (Object argument : arguments) {
                pstmt.setObject(count, argument);
                count++;
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... arguments) {
        log.debug("query : {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int count = START_ARGUMENT_COUNT;
            for (Object argument : arguments) {
                pstmt.setObject(count, argument);
                count++;
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> objects = new ArrayList<>();
                while (rs.next()) {
                    objects.add(rowMapper.mapRow(rs, rs.getFetchSize()));
                }
                return objects;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        log.debug("queryObject query : {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int count = 1;
            for (Object argument : arguments) {
                pstmt.setObject(count, argument);
                count++;
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs, rs.getFetchSize());
                }
                return null;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
