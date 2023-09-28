package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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

    public int update(String sql, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
