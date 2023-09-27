package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate<T> {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            setParameter(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            close(null, pstmt, conn);
        }
    }

    public T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            setParameter(pstmt, args);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            close(rs, pstmt, conn);
        }
    }

    public List<T> queryForList(String sql, RowMapper<T> mapper, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            setParameter(pstmt, args);
            rs = pstmt.executeQuery();

            List<T> results = new ArrayList<>();
            while (rs.next()) {
                T result = mapper.map(rs);
                results.add(result);
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            close(rs, pstmt, conn);
        }
    }

    private void setParameter(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i=0; i<args.length; i++) {
            Object value = args[i];
            if (value instanceof String) {
                pstmt.setString(i+1, (String) value);
            }
            if (value instanceof Long) {
                pstmt.setLong(i+1, (Long) value);
            }
        }
    }

    private void close(final ResultSet rs, final PreparedStatement pstmt, final Connection conn) {
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

    public DataSource getDataSource() {
        return dataSource;
    }
}
