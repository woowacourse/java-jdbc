package com.interface21.jdbc.core;

import com.interface21.jdbc.JdbcExecutionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public int update(final String sql, final Object... parameters) {
        return execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor, Object[] parameters) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}, params : {}", sql, Arrays.toString(parameters));
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error("executeUpdate failed. sql={}, params={}", sql, Arrays.toString(parameters), e);
            throw new JdbcExecutionException("쿼리 실행 실패 : " + e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public int update(final Connection connection, final String sql, final Object... parameters) {
        return execute(connection, sql, PreparedStatement::executeUpdate, parameters);
    }

    private <T> T execute(Connection conn, String sql, PreparedStatementExecutor<T> executor, Object[] parameters) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}, params : {}", sql, Arrays.toString(parameters));
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error("executeUpdate failed. sql={}, params={}", sql, Arrays.toString(parameters), e);
            throw new JdbcExecutionException("쿼리 실행 실패 : " + e.getMessage());
        }
    }

    private void setStatementParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, (psmt) -> {
            try (ResultSet rs = psmt.executeQuery()) {
                List<T> instances = new ArrayList<>();
                while (rs.next()) {
                    instances.add(rowMapper.mapForObject(rs));
                }
                return instances;
            }
        }, parameters);
    }

    public <T> T executeQueryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);
        if (results.isEmpty()) {
            throw new JdbcExecutionException("쿼리 실행 결과 없음");
        }
        if (results.size() > 1) {
            throw new JdbcExecutionException("쿼리 실행 결과가 2개 이상");
        }
        return results.getFirst();
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}
