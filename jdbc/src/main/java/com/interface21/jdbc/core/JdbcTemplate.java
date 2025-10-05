package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(
            final String sql,
            final PreparedStatementSetter pstmts
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            pstmts.setParameters(pstmt);
            pstmt.executeUpdate();

        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(
            final String sql,
            final PreparedStatementSetter pstmts,
            final RowMapper<T> rowMapper
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            pstmts.setParameters(pstmt);

            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rowMapper.mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
