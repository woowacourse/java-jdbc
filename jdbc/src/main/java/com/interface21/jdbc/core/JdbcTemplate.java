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

    public int executeUpdate(final String sql, final Object... parameters) {
        validateQuery(sql);
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            setStatementParameters(preparedStatement, parameters);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw dataAccessException("executeUpdate(conn) 실패", sql, parameters, e);
        }
    }

    public <T> Optional<T> executeQueryForObject(final String sql, final RowMapper<T> rowMapper,
                                                 final Object... parameters) {
        validateQuery(sql);
        if (rowMapper == null) {
            throw new IllegalArgumentException("RowMapper는 null일 수 없습니다.");
        }
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            setStatementParameters(preparedStatement, parameters);
            log.debug("query : {}", sql);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    T result = rowMapper.mapRow(resultSet);
                    if (resultSet.next()) {
                        throw new DataAccessException("결과가 2개 이상입니다. 단일 결과만 가능합니다.");
                    }
                    return Optional.of(result);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw dataAccessException("executeQueryForObject 실패", sql, parameters, e);
        }
    }

    public <T> List<T> executeQuery(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        validateQuery(sql);
        if (rowMapper == null) {
            throw new IllegalArgumentException("RowMapper는 null일 수 없습니다.");
        }
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            setStatementParameters(preparedStatement, parameters);
            log.debug("query : {}", sql);
            try (final ResultSet rs = preparedStatement.executeQuery()) {
                final List<T> instances = new ArrayList<>();
                while (rs.next()) {
                    instances.add(rowMapper.mapRow(rs));
                }
                return instances;
            }
        } catch (SQLException e) {
            throw dataAccessException("executeQuery 실패", sql, parameters, e);
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
