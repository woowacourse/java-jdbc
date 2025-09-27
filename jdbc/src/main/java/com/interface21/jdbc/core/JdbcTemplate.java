package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            log.debug("query : {}", sql);

            setParameters(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T getSingleResult(RowMapper<T> rowMapper, String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            setParameters(pstmt, args);

            try (ResultSet rs = pstmt.executeQuery()) {
                log.info("query : {}", sql);

                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return null;
    }

    public <T> List<T> getResults(RowMapper<T> rowMapper, String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            setParameters(pstmt, args);

            try (ResultSet rs = pstmt.executeQuery()) {
                log.info("query : {}", sql);

                List<T> result = new ArrayList<>();

                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }

                return result;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        if (args == null || args.length == 0) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
