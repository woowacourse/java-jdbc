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
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final Connection conn, final String sql, final Object... args) {
        executeQuery(conn, sql, PreparedStatement::executeUpdate, args);
    }

    public void update(final String sql, final Object... args) {
        executeQuery(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(sql, pstmt -> makeResults(rowMapper, pstmt), args);
    }

    private <T> List<T> makeResults(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        final ResultSet rs = pstmt.executeQuery();

        final List<T> results = new ArrayList<>();
        while (rs.next()) {
            final T mappedRow = rowMapper.mapRow(rs);
            results.add(mappedRow);
        }
        return results;
    }

    private <T> T executeQuery(
            final Connection conn,
            final String sql,
            final PreparedStatementExecutor<T> executor,
            final Object... args
    ) {
        try (
                final PreparedStatement pstmt = processPreparedStatement(conn, sql, args)
        ) {
            log.debug("query : {}", sql);

            return executor.execute(pstmt);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T executeQuery(
            final String sql,
            final PreparedStatementExecutor<T> executor,
            final Object... args
    ) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = processPreparedStatement(conn, sql, args)
        ) {
            log.debug("query : {}", sql);

            return executor.execute(pstmt);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement processPreparedStatement(
            final Connection conn,
            final String sql,
            final Object... args
    ) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        setPreparedStatement(pstmt, args);
        return pstmt;
    }

    private void setPreparedStatement(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
