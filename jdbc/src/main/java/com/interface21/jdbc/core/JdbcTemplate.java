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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... values) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPstmt(conn, sql, values)
        ) {
            pstmt.executeUpdate();
            log.debug("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryOne(String sql, ResultExtractor<T> re, Object... values) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPstmt(conn, sql, values)
        ) {
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            if (rs.next()) return re.extract(rs);
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    public <T> List<T> queryMany(String sql, ResultExtractor<T> re, Object... values) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPstmt(conn, sql, values)
        ) {
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();
            while (rs.next()) results.add(re.extract(rs));
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    private static PreparedStatement createPstmt(Connection conn, String sql, Object... values) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            pstmt.setObject(i + 1, values[i]);
        }
        return pstmt;
    }

    private static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
