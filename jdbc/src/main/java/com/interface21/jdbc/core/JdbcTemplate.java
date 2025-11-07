package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

    public void update(final String sql, final PreparedStatementSetter pss) {
        execute(sql, pstmt -> {
            pss.setValues(pstmt);
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        });
    }

    public void update(final String sql, final Object...args) {
        update(sql, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter pss
    ) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            log.debug("query : {}", sql);

            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            }
            return null;
        });
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object...args
    ) {
        return queryForObject(sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> List<T> queryForList(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter pss
    ) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            log.debug("query : {}", sql);

            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }

                if (results.isEmpty()) {
                    return Collections.emptyList();
                }
                return results;
            }
        });
    }

    public <T> List<T> queryForList(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object...args
    ) {
        return queryForList(sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        return queryForList(sql, rowMapper, pstmt -> {});
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementCallback<T> callBack
    ) {
        final Connection existingConn = TransactionSynchronizationManager.getResource(dataSource);
        final Connection conn = DataSourceUtils.getConnection(dataSource);

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return callBack.processIn(pstmt);
        } catch (final SQLException e) {
            log.error("JdbcTemplate execution failed. SQL: {}", sql, e);
            throw new DataAccessException("JdbcTemplate execution failed for SQL: " + sql, e);
        } finally {
            if (existingConn == null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
}
