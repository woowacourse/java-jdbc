package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void update(final String sql, final Object... args) {
        update(sql, createSetter(args));
    }

    public void update(final String sql, final PreparedStatementSetter setter) {
        execute(sql, pstmt -> {
            setter.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public void update(final Connection conn, final String sql, final Object... args) {
        execute(conn, sql, pstmt -> {
            createSetter(args).setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException("조회 결과가 2건 이상입니다. size: " + results.size());
        }
        return results.stream().findFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, createSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            setter.setValues(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        final boolean inTransaction = TransactionSynchronizationManager.getResource(dataSource) != null;
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            if (!inTransaction) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }

    private <T> T execute(final Connection conn, final String sql, final PreparedStatementCallback<T> callback) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter createSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }
}
