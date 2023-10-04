package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = getPreparedStatement(conn, sql, args)) {

            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = getPreparedStatement(conn, sql, args);
             ResultSet rs = pstmt.executeQuery()) {

            log.debug("query : {}", sql);

            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = getPreparedStatement(conn, sql, args);
             ResultSet rs = pstmt.executeQuery()) {

            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();
            while (rs.next()) {
                T result = mapper.map(rs);
                results.add(result);
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement getPreparedStatement(final Connection conn, final String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
}
