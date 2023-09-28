package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

