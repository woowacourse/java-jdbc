package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
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

    public Optional<Object> queryForObject(final String sql, final RowMapper rowMapper, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i].toString());
            }
            final ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs, 4));
            }
            return null;
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }
}
