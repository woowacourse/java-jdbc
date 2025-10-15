package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void update(final String sql, final Object... parameters) {
        execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        validateQuery(sql);
        return execute(sql, (preparedStatement) -> {
            try (final ResultSet rs = preparedStatement.executeQuery()) {
                final List<T> instances = new ArrayList<>();
                while (rs.next()) {
                    instances.add(rowMapper.mapRow(rs));
                }
                return instances;
            }
        }, parameters);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper,
                                          final Object... parameters) {
        if (rowMapper == null) {
            throw new IllegalArgumentException("RowMapper는 null일 수 없습니다.");
        }
        final List<T> results = query(sql, rowMapper, parameters);

        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DataAccessException("executeQueryForObject는 1행만 기대하지만 " + results.size() + "행을 반환했습니다.");
        }
        return Optional.ofNullable(results.getFirst());
    }

    private <T> T execute(final String sql, final PreparedStatementExecutor<T> executor, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement prepareStatement = conn.prepareStatement(sql)) {
            validateParameterCount(prepareStatement, parameters, sql);
            setStatementParameters(prepareStatement, parameters);
            log.debug("query : {}", sql);
            return executor.execute(prepareStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw dataAccessException("실패", sql, parameters, e);
        }
    }

    private void setStatementParameters(final PreparedStatement preparedStatement, final Object[] parameters)
            throws SQLException {
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
        }
    }

    private void validateParameterCount(PreparedStatement preparedStatement, Object[] params, String sql)
            throws SQLException {
        int expectedParameterCount = preparedStatement.getParameterMetaData().getParameterCount();
        int actualParameterCount = (params == null) ? 0 : params.length;
        if (expectedParameterCount != actualParameterCount) {
            throw new DataAccessException(
                    String.format("SQL 파라미터 개수 불일치 (expected=%d, actual=%d) sql=%s",
                            expectedParameterCount, actualParameterCount, sql)
            );
        }
    }

    private DataAccessException dataAccessException(String msg, String sql, Object[] params, SQLException e) {
        String paramsStr = (params != null) ? Arrays.toString(params) : "null";
        log.error("{} | sql={} | params={}", msg, sql, paramsStr, e);
        return new DataAccessException(msg + " : " + sql, e);
    }

    private static void validateQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL 쿼리는 null이거나 빈 문자열일 수 없습니다.");
        }
    }
}
