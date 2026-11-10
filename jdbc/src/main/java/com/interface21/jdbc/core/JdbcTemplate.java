package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return query(conn, sql, rowMapper, args);
        } finally {
            releaseIfNotTransactional(conn);
        }
    }

    public <T> List<T> query(final Connection conn, final String sql, final RowMapper<T> rowMapper,
        final Object... args) {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql);
            final ResultSet rs = executeQuery(createPreparedStatementSetter(args), pstmt)) {
            log.debug("query : {}", sql);

            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
        final Object... args) {
        return singleResult(query(sql, rowMapper, args));
    }

    public <T> T queryForObject(final Connection conn, final String sql,
        final RowMapper<T> rowMapper,
        final Object... args) {
        return singleResult(query(conn, sql, rowMapper, args));
    }

    public int update(final String sql, final Object... args) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return update(conn, sql, args);
        } finally {
            releaseIfNotTransactional(conn);
        }
    }

    public int update(final Connection conn, final String sql, final Object... args) {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            createPreparedStatementSetter(args).setValues(pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T singleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("결과가 없습니다.");
        }
        if (results.size() > 1) {
            throw new DataAccessException("결과가 1개보다 많습니다. size=" + results.size());
        }

        return results.get(0);
    }

    private ResultSet executeQuery(final PreparedStatementSetter pss,
        final PreparedStatement pstmt) throws SQLException {
        pss.setValues(pstmt);
        return pstmt.executeQuery();
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                    continue;
                }
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }

    private void releaseIfNotTransactional(final Connection connection) {
        try {
            if (connection.getAutoCommit()) {
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
