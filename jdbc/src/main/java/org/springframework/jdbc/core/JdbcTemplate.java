package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... params) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setCondition(params, pstmt);

            pstmt.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
                final ResultSet rs = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);

            final List<T> results = new ArrayList<>();

            while (rs.next()) {
                T object = rowMapper.mapRow(rs);
                results.add(object);
            }

            return results;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, RowMapper<T> rowMapper, final Object... params) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = getPreparedStatement(sql, conn, params);
                final ResultSet rs = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);


            while (rs.next()) {
                T object = rowMapper.mapRow(rs);
                return Optional.ofNullable(object);
            }

            return Optional.empty();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getPreparedStatement(final String sql, final Connection conn, final Object[] params) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        setCondition(params, pstmt);
        return pstmt;
    }

    private void setCondition(final Object[] params, final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

}
