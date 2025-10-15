package com.interface21.jdbc.core;

import com.interface21.jdbc.JdbcExecutionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void update(final String sql, final Object... parameters) {
        execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public void update(final Connection conn, final String sql, final Object... parameters) {
        execute(conn, sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, (pstmt) -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> instances = new ArrayList<>();
                while (rs.next()) {
                    instances.add(rowMapper.mapForObject(rs));
                }
                return instances;
            }
        }, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> results = query(sql, rowMapper, parameters);
        if (results.isEmpty()) {
            throw new JdbcExecutionException(String.format("\"%s\" 쿼리 실행 결과가 존재하지 않습니다.", sql));
        }
        if (results.size() > 1) {
            throw new JdbcExecutionException(String.format("\"%s\" 쿼리 실행 결과가 기대한 데이터 수와 같지 않습니다.", sql));
        }
        return results.getFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementExecutor<T> executor, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcExecutionException(String.format("\"%s\" 쿼리 실행 중 오류가 발생했습니다.", sql), e);
        }
    }

    private <T> T execute(final Connection conn, final String sql, final PreparedStatementExecutor<T> executor,
                          final Object... parameters) {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcExecutionException(String.format("\"%s\" 쿼리 실행 중 오류가 발생했습니다.", sql), e);
        }
    }

    private void setStatementParameters(final PreparedStatement pstmt, final Object[] parameters) throws SQLException {
        if (parameters == null) {
            throw new JdbcExecutionException(String.format("쿼리에 필요한 파라미터가 전달되지 않았습니다."));
        }
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }
}
