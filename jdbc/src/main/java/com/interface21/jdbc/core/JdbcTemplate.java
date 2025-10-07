package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
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

    public void update(final String sql, final Object... args) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = setValues(conn.prepareStatement(sql), args)) {
            log.debug("query : {}", sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = setValues(conn.prepareStatement(sql), args);
             final var rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = setValues(conn.prepareStatement(sql), args);
             final var rs = pstmt.executeQuery()) {
            final List<T> result = new ArrayList<>();
            log.debug("query : {}", sql);
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement setValues(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
}
