package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

abstract class AutoClosableTemplate {

    private static final Logger log = LoggerFactory.getLogger(AutoClosableTemplate.class);

    protected DataSource dataSource;

    AutoClosableTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final void execute(final String sql, final Object... params) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = getPreparedStatement(conn, sql, params)
        ) {
            log.debug("query : {}", sql);

            commandQuery(pstmt);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public final <T> List<T> query(final String sql, RowMapper<T> rowMapper) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
                final ResultSet rs = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);

            return queryAll(rs, rowMapper);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public final <T> Optional<T> queryForObject(final String sql, RowMapper<T> rowMapper, final Object... params) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = getPreparedStatement(conn, sql, params);
                final ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);

            return queryForOne(rs, rowMapper);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getPreparedStatement(final Connection conn, final String sql, final Object[] params) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }

        return pstmt;
    }

    protected abstract void commandQuery(final PreparedStatement pstmt) throws SQLException;

    protected abstract <T> List<T> queryAll(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException;

    protected abstract <T> Optional<T> queryForOne(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException;
}
