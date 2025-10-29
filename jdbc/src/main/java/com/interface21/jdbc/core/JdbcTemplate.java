package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.ParameterBindingException;
import com.interface21.jdbc.exception.QueryExecutionException;
import com.interface21.jdbc.exception.ResultSetProcessingException;
import com.interface21.jdbc.exception.RowMappingException;
import com.interface21.jdbc.exception.StatementPreparationException;
import com.interface21.jdbc.exception.UpdateExecutionException;
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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, PreparedStatementSetter setter) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        log.debug("query : {}", sql);
        try (final var pstmt = getPreparedStatement(sql, connection)) {
            bindParams(sql, pstmt, setter);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UpdateExecutionException(e, sql);
        } finally {
            if (TransactionSynchronizationManager.getResource(dataSource) == null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    public <T> List<T> query(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        final var result = new ArrayList<T>();
        log.debug("query : {}", sql);
        try (final var pstmt = getPreparedStatement(sql, connection);
             final var rs = executeQuery(sql, pstmt, setter)) {
            while (rs.next()) {
                result.add(mapRow(rowMapper, rs, sql));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ResultSetProcessingException(e, sql);
        } finally {
            if (TransactionSynchronizationManager.getResource(dataSource) == null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        log.debug("query : {}", sql);
        try (final var pstmt = getPreparedStatement(sql, connection);
             final var rs = executeQuery(sql, pstmt, setter)) {
            if (rs.next()) {
                return mapRow(rowMapper, rs, sql);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ResultSetProcessingException(e, sql);
        } finally {
            if (TransactionSynchronizationManager.getResource(dataSource) == null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection conn) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StatementPreparationException(e, sql);
        }
    }

    private ResultSet executeQuery(String sql, PreparedStatement pstmt, PreparedStatementSetter setter) {
        try {
            bindParams(sql, pstmt, setter);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueryExecutionException(e, sql);
        }
    }

    private <T> T mapRow(RowMapper<T> rowMapper, ResultSet rs, String sql) {
        try {
            return rowMapper.apply(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RowMappingException(e, sql);
        }
    }

    private void bindParams(String sql, PreparedStatement pstmt, PreparedStatementSetter setter) {
        try {
            setter.accept(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ParameterBindingException(e, sql);
        }
    }
}
