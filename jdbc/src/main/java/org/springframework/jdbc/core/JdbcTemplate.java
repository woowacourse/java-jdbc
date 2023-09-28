package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i].toString());
            }
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql);
             final ResultSet rs = getResultSet(pstmt, params)) {

            log.debug("query : {}", sql);

            final List<T> results = new ArrayList<>();

            if (rs.next()) {
                results.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return results;
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql);
             final ResultSet rs = getResultSet(pstmt, params)) {

            log.debug("query : {}", sql);

            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs, rs.getRow()));
            }
            return null;
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }

    private ResultSet getResultSet(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setString(i + 1, params[i].toString());
        }
        return pstmt.executeQuery();
    }
}
