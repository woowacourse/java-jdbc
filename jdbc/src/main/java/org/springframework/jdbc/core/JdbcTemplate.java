package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.exception.IncorrectResultSizeDataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            final int argsLength = args.length;
            for (int i = 0; i < argsLength; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMaper, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            final int argsLength = args.length;
            for (int i = 0; i < argsLength; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();
            if (rs.next()) {
                results.add(rowMaper.mapRow(rs, rs.getRow()));
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            final int argsLength = args.length;
            for (int i = 0; i < argsLength; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            T findObject = null;
            if (rs.next()) {
                findObject = rowMapper.mapRow(rs, rs.getRow());
            }
            if (findObject == null) {
                throw new IncorrectResultSizeDataAccessException(0);
            }
            if (rs.next()) {
                rs.last();
                throw new IncorrectResultSizeDataAccessException(rs.getRow());
            }

            return findObject;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }
}
