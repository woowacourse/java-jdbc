package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, Object... obj) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            for (int i = 0; i < obj.length; i++) {
                setSQLParameter(obj[i], i + 1, pstmt);
            }
            pstmt.execute();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static void setSQLParameter(final Object parameter, final int parameterIndex,
                                        final PreparedStatement pstmt)
            throws SQLException {
        if (parameter instanceof String) {
            pstmt.setString(parameterIndex, (String) parameter);
            return;
        }
        if (parameter instanceof Long) {
            pstmt.setLong(parameterIndex, (Long) parameter);
            return;
        }
        if (parameter instanceof Double) {
            pstmt.setDouble(parameterIndex, (Double) parameter);
            return;
        }
        if (parameter instanceof LocalDateTime) {
            pstmt.setTimestamp(parameterIndex, Timestamp.valueOf((LocalDateTime) parameter));
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... obj) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            for (int i = 0; i < obj.length; i++) {
                setSQLParameter(obj[i], i + 1, pstmt);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowMapper.mapRow(rs, rs.getRow());
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... obj) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            for (int i = 0; i < obj.length; i++) {
                setSQLParameter(obj[i], i + 1, pstmt);
            }
            ResultSet rs = pstmt.executeQuery();
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
