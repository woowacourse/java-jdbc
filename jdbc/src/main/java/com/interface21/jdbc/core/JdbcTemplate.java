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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final void update(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public final <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapResults(rowMapper, rs);
            }
        }, params);
    }

    public final <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.getFirst());
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor, Object... params) {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == null) {
            return executeWithNewConn(sql, executor, params);
        }
        Connection aliveConn = DataSourceUtils.getConnection(dataSource);
        return executeWithConn(aliveConn, sql, executor, params);
    }

    private <T> T executeWithNewConn(String sql, PreparedStatementExecutor<T> executor, Object... params) {
        try (Connection conn = dataSource.getConnection()) {
            return executeWithConn(conn, sql, executor, params);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeWithConn(Connection conn, String sql, PreparedStatementExecutor<T> executor,
                                  Object... params) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParams(pstmt, params);
            return executor.apply(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        validateParamsCount(pstmt, params);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    private void validateParamsCount(PreparedStatement pstmt, Object... params) throws SQLException {
        int paramsCount = pstmt.getParameterMetaData().getParameterCount();
        if (paramsCount != params.length) {
            throw new SQLException("파라미터 개수가 일치하지 않습니다.");
        }
    }

    private <T> List<T> mapResults(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> values = new ArrayList<>();
        while (rs.next()) {
            values.add(rowMapper.mapRow(rs));
        }
        return values;
    }
}
