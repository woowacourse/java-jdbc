package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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

    public int update(final String sql, final Object... args) {
        return update(sql, createSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> pstmt.executeUpdate());
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException("[ERROR] 결과가 " + results.size() + "건이 조회되었습니다.");
        }
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.getFirst());
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, createSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    private PreparedStatementSetter createSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }

    // 보관함(TransactionSynchronizationManager)에서 커넥션을 얻어 실행
    private <T> T execute(final String sql, final PreparedStatementSetter pss,
                          final PreparedStatementCallback<T> action) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return execute(conn, sql, pss, action);
        } finally {
            releaseIfNotInTransaction(conn);
        }
    }

    // 트랜잭션 중(autoCommit=false)이면 서비스가 정리하므로 그대로 두고,
    // 트랜잭션 밖이면 JdbcTemplate이 직접 닫고 보관함에서도 뺀다
    private void releaseIfNotInTransaction(final Connection conn) {
        try {
            if (conn.getAutoCommit()) {
                DataSourceUtils.releaseConnection(conn, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    // 받은 커넥션으로 실행만 하고, 커넥션은 닫지 않음
    private <T> T execute(final Connection conn, final String sql, final PreparedStatementSetter pss,
                          final PreparedStatementCallback<T> action) {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
